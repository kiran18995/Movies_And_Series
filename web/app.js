/* ──────────────────────────────────────────────────────────────
   Movies & Series — Web App
   Powered by TMDB API
   ────────────────────────────────────────────────────────────── */

'use strict';

// ── CONFIG ────────────────────────────────────────────────────────────────
const API_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyZTZlYjBkNTE3OWY3MDMxYWNmMzE0ZGI4ZTQxMTJhOSIsIm5iZiI6MTcxOTI1ODUwMi42NTkxMzIsInN1YiI6IjVjYTFiMmNkOTI1MTQxMWExODA4ZDEyZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.xjTYKsio_c1M2zxwtfdpOgSyGthyjnsvzRh3ifbBFYg';
const BASE_URL   = 'https://api.themoviedb.org/3';
const IMG_BASE   = 'https://image.tmdb.org/t/p/';
const HEADERS    = { Authorization: `Bearer ${API_TOKEN}`, 'Content-Type': 'application/json' };

// ── STATE ─────────────────────────────────────────────────────────────────
let state = {
  tab:         'movies',     // 'movies' | 'tvshows' | 'saved'
  query:       '',
  sortBy:      'popularity.desc',
  language:    '',
  genre:       '',
  page:        1,
  totalPages:  1,
  loading:     false,
  items:       [],
  bookmarks:   loadBookmarks(),
  heroItems:   [],
  heroIndex:   0,
  heroTimer:   null,
  genres:      { movies: [], tv: [] },
  debounceTimer: null,
  currentDetail: null,
  seasonData:  null,
  selSeason:   1,
  selEpisode:  1,
};

// ── STORAGE ───────────────────────────────────────────────────────────────
function loadBookmarks() {
  try { return JSON.parse(localStorage.getItem('bookmarks') || '[]'); }
  catch { return []; }
}
function saveBookmarks() {
  localStorage.setItem('bookmarks', JSON.stringify(state.bookmarks));
}
function isBookmarked(id) {
  return state.bookmarks.some(b => b.id === id);
}
function toggleBookmark(item) {
  const idx = state.bookmarks.findIndex(b => b.id === item.id);
  if (idx >= 0) {
    state.bookmarks.splice(idx, 1);
    showToast('🗑 Removed from Saved', 'red');
  } else {
    state.bookmarks.unshift(item);
    showToast('🔖 Added to Saved', 'gold');
  }
  saveBookmarks();
  // refresh bookmark buttons in grid
  document.querySelectorAll(`.card-bookmark[data-id="${item.id}"]`).forEach(btn => {
    btn.textContent = isBookmarked(item.id) ? '🔖' : '🏷';
    btn.classList.toggle('bookmarked', isBookmarked(item.id));
  });
  // refresh modal bookmark btn
  const mb = document.getElementById('modalBookmarkBtn');
  if (mb && Number(mb.dataset.id) === item.id) {
    mb.classList.toggle('active', isBookmarked(item.id));
    mb.innerHTML = isBookmarked(item.id) ? '🔖 Saved' : '🏷 Save';
  }
  if (state.tab === 'saved') renderSaved();
}

// ── API HELPERS ───────────────────────────────────────────────────────────
async function apiFetch(path, params = {}) {
  const url = new URL(`${BASE_URL}${path}`);
  Object.entries(params).forEach(([k, v]) => { if (v !== '' && v != null) url.searchParams.set(k, v); });
  const res = await fetch(url, { headers: HEADERS });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}

// ── GENRES ────────────────────────────────────────────────────────────────
async function loadGenres() {
  const [mg, tg] = await Promise.all([
    apiFetch('/genre/movie/list'),
    apiFetch('/genre/tv/list'),
  ]);
  state.genres.movies = mg.genres || [];
  state.genres.tv     = tg.genres || [];
  populateGenreSelect();
}
function populateGenreSelect() {
  const sel  = document.getElementById('genreSelect');
  const list = state.tab === 'tvshows' ? state.genres.tv : state.genres.movies;
  sel.innerHTML = '<option value="">All Genres</option>';
  list.forEach(g => {
    const o = document.createElement('option');
    o.value = g.id; o.textContent = g.name;
    sel.appendChild(o);
  });
  sel.value = state.genre;
}

// ── FETCH ITEMS ───────────────────────────────────────────────────────────
async function fetchItems(reset = false) {
  if (state.loading) return;
  if (reset) {
    state.page  = 1;
    state.items = [];
    showSkeleton(true);
    showGrid(false);
    showEmpty(false);
  }

  state.loading = true;
  const isTV    = state.tab === 'tvshows';
  const type    = isTV ? 'tv' : 'movie';

  try {
    let data;
    if (state.query.trim()) {
      data = await apiFetch(`/search/${type}`, {
        query:               state.query,
        page:                state.page,
        language:            'en-US',
        with_original_language: state.language || undefined,
      });
    } else {
      data = await apiFetch(`/discover/${type}`, {
        sort_by:              state.sortBy,
        page:                 state.page,
        language:             'en-US',
        with_original_language: state.language || undefined,
        with_genres:          state.genre || undefined,
        'vote_count.gte':     state.sortBy === 'vote_average.desc' ? 300 : undefined,
      });
    }

    state.totalPages = Math.min(data.total_pages || 1, 500);
    const newItems   = (data.results || []).map(r => normalise(r, !isTV));

    if (reset) {
      state.items = newItems;
      renderGrid(true);
      // Set hero from first page results
      if (state.page === 1 && newItems.length > 0) {
        state.heroItems = newItems.slice(0, 5);
        state.heroIndex = 0;
        renderHero();
        startHeroTimer();
      }
    } else {
      state.items = [...state.items, ...newItems];
      renderGrid(false);
    }

    updateResultsCount();
    showSkeleton(false);
    showGrid(state.items.length > 0);
    if (state.items.length === 0) showEmpty(true, '🔍', 'No results found', 'Try a different search or filter.');

  } catch (err) {
    console.error(err);
    showSkeleton(false);
    showEmpty(true, '⚠️', 'Failed to load', err.message);
  } finally {
    state.loading = false;
  }
}

function normalise(r, isMovie) {
  return {
    id:           r.id,
    title:        r.title || r.name || 'Unknown',
    overview:     r.overview || '',
    posterPath:   r.poster_path,
    backdropPath: r.backdrop_path,
    voteAverage:  r.vote_average || 0,
    voteCount:    r.vote_count   || 0,
    releaseDate:  r.release_date || r.first_air_date || '',
    language:     r.original_language || '',
    isMovie,
    genres:       r.genre_ids || [],
  };
}

// ── RENDER HERO ───────────────────────────────────────────────────────────
function renderHero() {
  const items = state.heroItems;
  if (!items.length) return;
  const item  = items[state.heroIndex];

  const backdrop = document.getElementById('heroBackdrop');
  if (item.backdropPath) {
    backdrop.style.backgroundImage = `url(${IMG_BASE}w1280${item.backdropPath})`;
  }

  document.getElementById('heroTitle').textContent    = item.title;
  document.getElementById('heroOverview').textContent = item.overview;

  const year   = item.releaseDate ? item.releaseDate.slice(0, 4) : '';
  const rating = item.voteAverage ? item.voteAverage.toFixed(1) : '';
  document.getElementById('heroMeta').innerHTML = `
    ${rating ? `<span class="rating-pill">⭐ ${rating}</span>` : ''}
    ${year ? `<span>${year}</span>` : ''}
    <span>${item.isMovie ? '🎥 Movie' : '📺 TV Show'}</span>
  `;

  document.getElementById('heroBadge').textContent = item.isMovie ? '🔥 Popular Movie' : '🔥 Popular Show';

  // dots
  const dotsEl = document.getElementById('heroDots');
  dotsEl.innerHTML = '';
  items.forEach((_, i) => {
    const d = document.createElement('div');
    d.className = 'hero-dot' + (i === state.heroIndex ? ' active' : '');
    d.addEventListener('click', () => { state.heroIndex = i; renderHero(); });
    dotsEl.appendChild(d);
  });

  // wire hero buttons
  document.getElementById('heroPlayBtn').onclick = () => openPlayer(item);
  document.getElementById('heroInfoBtn').onclick = () => openDetail(item);
}

function startHeroTimer() {
  clearInterval(state.heroTimer);
  state.heroTimer = setInterval(() => {
    if (!state.heroItems.length) return;
    state.heroIndex = (state.heroIndex + 1) % state.heroItems.length;
    renderHero();
  }, 5000);
}

// ── RENDER GRID ───────────────────────────────────────────────────────────
function renderGrid(reset) {
  const grid = document.getElementById('itemGrid');
  if (reset) grid.innerHTML = '';

  const items = state.tab === 'saved' ? [] : state.items;
  const startIdx = reset ? 0 : grid.children.length;
  items.slice(startIdx).forEach((item, i) => {
    const card = buildCard(item, i * 30);
    grid.appendChild(card);
  });
}

function renderSaved() {
  showEmpty(false);
  showGrid(false);
  const grid = document.getElementById('itemGrid');
  grid.innerHTML = '';

  if (state.bookmarks.length === 0) {
    showEmpty(true, '🔖', 'No saved items', 'Bookmark movies and shows to find them here.');
    return;
  }
  state.bookmarks.forEach((item, i) => {
    grid.appendChild(buildCard(item, i * 30));
  });
  showGrid(true);
  updateResultsCount();
}

function buildCard(item, delay = 0) {
  const card = document.createElement('div');
  card.className = 'card';
  card.style.animationDelay = `${delay}ms`;
  card.dataset.id = item.id;

  const posterUrl = item.posterPath
    ? `${IMG_BASE}w342${item.posterPath}`
    : 'https://via.placeholder.com/342x513/16161f/8888aa?text=No+Image';

  const year = item.releaseDate ? item.releaseDate.slice(0, 4) : '';
  const bm   = isBookmarked(item.id);

  card.innerHTML = `
    <img src="${posterUrl}" alt="${escHtml(item.title)}" loading="lazy" />
    <div class="card-overlay"></div>
    <div class="card-type">${item.isMovie ? '🎥' : '📺'}</div>
    <button class="card-bookmark ${bm ? 'bookmarked' : ''}" data-id="${item.id}" title="${bm ? 'Remove bookmark' : 'Bookmark'}">${bm ? '🔖' : '🏷'}</button>
    <div class="card-info">
      <div class="card-title">${escHtml(item.title)}</div>
      <div class="card-meta">
        <span class="card-rating">⭐ ${item.voteAverage ? item.voteAverage.toFixed(1) : 'N/A'}</span>
        ${year ? `<span>${year}</span>` : ''}
      </div>
    </div>
  `;

  card.querySelector('.card-bookmark').addEventListener('click', e => {
    e.stopPropagation();
    toggleBookmark(item);
  });

  card.addEventListener('click', () => openDetail(item));
  return card;
}

// ── DETAIL MODAL ──────────────────────────────────────────────────────────
async function openDetail(item) {
  state.currentDetail = item;
  const overlay = document.getElementById('modalOverlay');
  overlay.classList.remove('hidden');
  document.body.style.overflow = 'hidden';

  // set backdrop immediately
  const bdEl = document.getElementById('modalBackdrop');
  bdEl.src = item.backdropPath
    ? `${IMG_BASE}w1280${item.backdropPath}`
    : (item.posterPath ? `${IMG_BASE}w780${item.posterPath}` : '');

  // show spinner in body
  const body = document.getElementById('modalBody');
  body.innerHTML = `
    <div class="modal-header">
      <img class="modal-poster" src="${item.posterPath ? IMG_BASE + 'w342' + item.posterPath : ''}" alt="${escHtml(item.title)}" />
      <div class="modal-title-block">
        <h2 class="modal-title" id="modalTitle">${escHtml(item.title)}</h2>
        <p style="color:var(--text-muted);font-size:13px;margin-top:6px;">Loading details…</p>
      </div>
    </div>
  `;

  try {
    const type    = item.isMovie ? 'movie' : 'tv';
    const details = await apiFetch(`/${type}/${item.id}`, { append_to_response: 'credits,videos,seasons' });
    renderModal(item, details);
  } catch (e) {
    body.innerHTML += `<p style="color:var(--red);padding:20px">Failed to load details: ${e.message}</p>`;
  }
}

function renderModal(item, d) {
  const body   = document.getElementById('modalBody');
  const bm     = isBookmarked(item.id);
  const year   = (d.release_date || d.first_air_date || '').slice(0, 4);
  const rating = d.vote_average ? d.vote_average.toFixed(1) : 'N/A';
  const genres = (d.genres || []).map(g => `<span class="badge badge-genre">${g.name}</span>`).join('');
  const ratingColor = d.vote_average >= 7.5 ? '#4caf50' : d.vote_average >= 6 ? '#ffc107' : '#f44336';

  // Runtime / seasons
  let meta = '';
  if (d.runtime)            meta += `<span><strong>${d.runtime}</strong> min</span>`;
  if (d.number_of_seasons)  meta += `<span><strong>${d.number_of_seasons}</strong> Season${d.number_of_seasons > 1 ? 's' : ''}</span>`;
  if (d.number_of_episodes) meta += `<span><strong>${d.number_of_episodes}</strong> Episodes</span>`;
  if (d.status)             meta += `<span class="badge badge-status">${d.status}</span>`;

  // Trailer
  const trailer = (d.videos?.results || []).find(v => v.type === 'Trailer' && v.site === 'YouTube');

  // Cast
  const cast = (d.credits?.cast || []).slice(0, 10).map(c => `
    <div class="cast-card">
      <img class="cast-photo" src="${c.profile_path ? IMG_BASE + 'w185' + c.profile_path : 'https://via.placeholder.com/60x60/1e1e2a/8888aa?text=?'}" alt="${escHtml(c.name)}" loading="lazy" />
      <div class="cast-name">${escHtml(c.name)}</div>
    </div>
  `).join('');

  // Store details for episode picker
  state.seasonData = d.seasons || null;

  body.innerHTML = `
    <div class="modal-header">
      <img class="modal-poster" src="${item.posterPath ? IMG_BASE + 'w342' + item.posterPath : ''}" alt="${escHtml(item.title)}" />
      <div class="modal-title-block">
        <h2 class="modal-title" id="modalTitle">${escHtml(d.title || d.name || item.title)}</h2>
        <div class="modal-badges">
          <span class="badge badge-rating">⭐ ${rating}</span>
          ${year ? `<span class="badge badge-year">${year}</span>` : ''}
          ${genres}
        </div>
        ${d.tagline ? `<p class="modal-tagline">"${escHtml(d.tagline)}"</p>` : ''}
        <div class="modal-meta-row">${meta}</div>
      </div>
    </div>

    <div class="modal-actions">
      <button class="btn-play" id="modalPlayBtn">▶ Play Now</button>
      ${trailer ? `<a class="btn-trailer" href="https://www.youtube.com/watch?v=${trailer.key}" target="_blank" rel="noopener">🎞 Trailer</a>` : ''}
      <button class="btn-bookmark-modal ${bm ? 'active' : ''}" id="modalBookmarkBtn" data-id="${item.id}">${bm ? '🔖 Saved' : '🏷 Save'}</button>
    </div>

    ${d.overview ? `
    <div class="modal-section">
      <p class="modal-section-title">Overview</p>
      <p class="modal-overview">${escHtml(d.overview)}</p>
    </div>` : ''}

    ${cast ? `
    <div class="modal-section">
      <p class="modal-section-title">Cast</p>
      <div class="cast-row">${cast}</div>
    </div>` : ''}
  `;

  document.getElementById('modalPlayBtn').addEventListener('click', () => openPlayer(item));
  document.getElementById('modalBookmarkBtn').addEventListener('click', () => toggleBookmark(item));
}

function closeModal() {
  document.getElementById('modalOverlay').classList.add('hidden');
  document.body.style.overflow = '';
  state.currentDetail = null;
}

// ── PLAYER SOURCES ───────────────────────────────────────────────────────
// Server 1 = streamimdb.ru via local reverse proxy (same as Android app WebView).
//   URL uses /stream/* which the Node server proxies → strips X-Frame-Options.
// Servers 2-5 = external fallbacks that open in a new tab.
const MOVIE_SOURCES = [
  { label: 'Server 1', badge: 'Built-in', proxy: true,
    url: id => `https://streamimdb.ru/embed/movie/${id}`,
    extUrl: id => `https://streamimdb.ru/embed/movie/${id}` },
  { label: 'Server 2', proxy: false,
    url: id => `https://vidsrc.me/embed/movie?tmdb=${id}` },
  { label: 'Server 3', proxy: false,
    url: id => `https://streamimdb.ru/embed/movie/${id}` },
  { label: 'Server 4', proxy: false,
    url: id => `https://moviesapi.club/movie/${id}` },
  { label: 'Server 5', proxy: false,
    url: id => `https://smashystream.xyz/playere.php?tmdb=${id}&type=movie` },
];
const TV_SOURCES = [
  { label: 'Server 1', badge: 'Built-in', proxy: true,
    url: (id, s, e) => `https://streamimdb.ru/embed/tv/${id}`,
    extUrl: (id, s, e) => `https://streamimdb.ru/embed/tv/${id}` },
  { label: 'Server 2', proxy: false,
    url: (id, s, e) => `https://vidsrc.me/embed/tv?tmdb=${id}&season=${s}&episode=${e}` },
  { label: 'Server 3', proxy: false,
    url: (id, s, e) => `https://streamimdb.ru/embed/tv/${id}` },
  { label: 'Server 4', proxy: false,
    url: (id, s, e) => `https://moviesapi.club/tv/${id}-${s}-${e}` },
  { label: 'Server 5', proxy: false,
    url: (id, s, e) => `https://smashystream.xyz/playere.php?tmdb=${id}&type=tv&s=${s}&e=${e}` },
];

let currentPlayerItem    = null;
let currentPlayerSeason  = 1;
let currentPlayerEpisode = 1;
let activeSourceIdx      = 0;
let playerIsTV           = false;

// ── PLAYER ────────────────────────────────────────────────────────────────
function openPlayer(item) {
  if (!item.isMovie) {
    openEpisodePicker(item);
    return;
  }
  launchPlayer(item.title || item.name, false, item, 1, 1);
}

function launchPlayer(title, isTV, item, season, episode) {
  const url = item.isMovie 
    ? `https://streamimdb.ru/embed/movie/${item.id}` 
    : `https://streamimdb.ru/embed/tv/${item.id}/${season}/${episode}`;

  document.getElementById('playerTitle').textContent = title;
  
  // Hide external prompt, hide tabs
  const tabs = document.getElementById('playerTabs');
  if(tabs) tabs.style.display = 'none';
  const external = document.getElementById('playerExternal');
  if(external) external.style.display = 'none';

  const frameWrap = document.getElementById('playerFrameWrap');
  frameWrap.classList.remove('hidden');
  
  // Show loader
  const loader = document.getElementById('iframeLoader');
  if(loader) loader.classList.remove('hidden');
  
  const frame = document.getElementById('playerFrame');
  frame.src = '';
  setTimeout(() => { frame.src = url; }, 60);
  
  document.getElementById('playerOverlay').classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closePlayer() {
  document.getElementById('playerOverlay').classList.add('hidden');
  const frame = document.getElementById('playerFrame');
  if (frame) frame.src = '';
  activeSourceIdx = 0;
  if (!document.getElementById('modalOverlay').classList.contains('hidden')) return;
  document.body.style.overflow = '';
}

// ── EPISODE PICKER ────────────────────────────────────────────────────────
function openEpisodePicker(item) {
  state.currentDetail = item;
  state.selSeason  = 1;
  state.selEpisode = 1;

  const seasons = state.seasonData
    ? state.seasonData.filter(s => s.season_number > 0).sort((a, b) => a.season_number - b.season_number)
    : [{ season_number: 1, episode_count: 20, name: 'Season 1' }];

  renderSeasonChips(seasons);
  renderEpisodeChips(seasons[0]?.episode_count || 20);

  document.getElementById('episodeOverlay').classList.remove('hidden');
  document.body.style.overflow = 'hidden';

  document.getElementById('playEpisodeBtn').onclick = () => {
    currentPlayerItem    = item;
    currentPlayerSeason  = state.selSeason;
    currentPlayerEpisode = state.selEpisode;
    activeSourceIdx      = 0;
    closeEpisodePicker();
    launchPlayer(`${item.title} — S${state.selSeason}E${state.selEpisode}`, true, item, state.selSeason, state.selEpisode);
  };
}


function renderSeasonChips(seasons) {
  const row = document.getElementById('seasonChips');
  row.innerHTML = '';
  seasons.forEach(s => {
    const c = document.createElement('button');
    c.className = 'chip' + (s.season_number === state.selSeason ? ' active' : '');
    c.textContent = `S${s.season_number}`;
    c.onclick = () => {
      state.selSeason  = s.season_number;
      state.selEpisode = 1;
      row.querySelectorAll('.chip').forEach(ch => ch.classList.remove('active'));
      c.classList.add('active');
      renderEpisodeChips(s.episode_count || 20);
    };
    row.appendChild(c);
  });
}

function renderEpisodeChips(count) {
  const row = document.getElementById('episodeChips');
  row.innerHTML = '';
  for (let i = 1; i <= count; i++) {
    const c = document.createElement('button');
    c.className = 'chip' + (i === state.selEpisode ? ' active' : '');
    c.textContent = i;
    c.onclick = () => {
      state.selEpisode = i;
      row.querySelectorAll('.chip').forEach(ch => ch.classList.remove('active'));
      c.classList.add('active');
    };
    row.appendChild(c);
  }
}

function closeEpisodePicker() {
  document.getElementById('episodeOverlay').classList.add('hidden');
  document.body.style.overflow = '';
}

// ── UI HELPERS ─────────────────────────────────────────────────────────────
function showSkeleton(v) {
  document.getElementById('skeletonGrid').classList.toggle('hidden', !v);
}
function showGrid(v) {
  document.getElementById('itemGrid').classList.toggle('hidden', !v);
}
function showEmpty(v, icon = '🎬', title = 'Nothing here', sub = '') {
  const el = document.getElementById('emptyState');
  el.classList.toggle('hidden', !v);
  if (v) {
    document.getElementById('emptyIcon').textContent    = icon;
    document.getElementById('emptyTitle').textContent   = title;
    document.getElementById('emptySubtitle').textContent = sub;
  }
}
function updateResultsCount() {
  const el = document.getElementById('resultsCount');
  if (state.tab === 'saved') {
    el.textContent = `${state.bookmarks.length} saved item${state.bookmarks.length !== 1 ? 's' : ''}`;
  } else if (state.items.length) {
    el.textContent = `${state.items.length} items loaded`;
  } else {
    el.textContent = '';
  }
}
function escHtml(s) {
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// ── TOAST ─────────────────────────────────────────────────────────────────
function showToast(msg, type = '') {
  const c    = document.getElementById('toastContainer');
  const t    = document.createElement('div');
  t.className = `toast${type ? ' toast-' + type : ''}`;
  t.textContent = msg;
  c.appendChild(t);
  setTimeout(() => {
    t.classList.add('removing');
    setTimeout(() => t.remove(), 260);
  }, 2400);
}

// ── INFINITE SCROLL ───────────────────────────────────────────────────────
function setupInfiniteScroll() {
  const trigger = document.getElementById('loadMoreTrigger');
  const obs = new IntersectionObserver(entries => {
    if (!entries[0].isIntersecting) return;
    if (state.tab === 'saved') return;
    if (state.loading) return;
    if (state.page >= state.totalPages) return;
    state.page++;
    fetchItems(false);
  }, { rootMargin: '300px' });
  obs.observe(trigger);
}

// ── TAB SWITCH ────────────────────────────────────────────────────────────
function switchTab(tab) {
  state.tab   = tab;
  state.query = '';
  state.genre = '';
  state.page  = 1;
  document.getElementById('searchInput').value = '';
  document.getElementById('searchClear').classList.remove('visible');

  document.querySelectorAll('.nav-tab').forEach(t => t.classList.toggle('active', t.dataset.tab === tab));

  const filterBar = document.getElementById('filterBar');
  filterBar.classList.toggle('hidden', tab === 'saved');

  showSkeleton(false);
  showEmpty(false);
  showGrid(false);
  document.getElementById('itemGrid').innerHTML = '';

  clearInterval(state.heroTimer);
  const hero = document.getElementById('heroBanner');

  if (tab === 'saved') {
    hero.classList.add('hidden');
    renderSaved();
  } else {
    hero.classList.remove('hidden');
    populateGenreSelect();
    fetchItems(true);
  }
}

// ── SEARCH ────────────────────────────────────────────────────────────────
function onSearch(q) {
  state.query = q;
  clearTimeout(state.debounceTimer);
  state.debounceTimer = setTimeout(() => {
    if (state.tab !== 'saved') fetchItems(true);
  }, 300);
  document.getElementById('searchClear').classList.toggle('visible', q.length > 0);
}

// ── INIT ──────────────────────────────────────────────────────────────────
function init() {
  // Tab buttons
  document.querySelectorAll('.nav-tab').forEach(btn => {
    btn.addEventListener('click', () => switchTab(btn.dataset.tab));
  });

  // Search
  const searchInput = document.getElementById('searchInput');
  searchInput.addEventListener('input', e => onSearch(e.target.value));
  document.getElementById('searchClear').addEventListener('click', () => {
    searchInput.value = '';
    onSearch('');
    searchInput.focus();
  });

  // Filters
  document.getElementById('sortSelect').addEventListener('change', e => {
    state.sortBy = e.target.value;
    if (state.tab !== 'saved') fetchItems(true);
  });
  document.getElementById('langSelect').addEventListener('change', e => {
    state.language = e.target.value;
    if (state.tab !== 'saved') fetchItems(true);
  });
  document.getElementById('genreSelect').addEventListener('change', e => {
    state.genre = e.target.value;
    if (state.tab !== 'saved') fetchItems(true);
  });

  // Modal close
  document.getElementById('modalClose').addEventListener('click', closeModal);
  document.getElementById('modalOverlay').addEventListener('click', e => {
    if (e.target === document.getElementById('modalOverlay')) closeModal();
  });

  // Player close
  document.getElementById('playerClose').addEventListener('click', closePlayer);
  document.getElementById('playerOverlay').addEventListener('click', e => {
    if (e.target === document.getElementById('playerOverlay')) closePlayer();
  });

  // Episode picker close
  document.getElementById('episodeClose').addEventListener('click', closeEpisodePicker);
  document.getElementById('episodeOverlay').addEventListener('click', e => {
    if (e.target === document.getElementById('episodeOverlay')) closeEpisodePicker();
  });

  // Keyboard
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
      if (!document.getElementById('playerOverlay').classList.contains('hidden'))  { closePlayer(); return; }
      if (!document.getElementById('episodeOverlay').classList.contains('hidden')) { closeEpisodePicker(); return; }
      if (!document.getElementById('modalOverlay').classList.contains('hidden'))   { closeModal(); return; }
    }
  });

  // Infinite scroll
  setupInfiniteScroll();

  // Load genres then first page
  loadGenres().catch(console.error);
  fetchItems(true);
}

document.addEventListener('DOMContentLoaded', init);

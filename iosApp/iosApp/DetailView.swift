import SwiftUI
import SafariServices
import shared

final class DetailViewModelState: ObservableObject {
    @Published var details: ItemDetails? = nil
    
    private let vm = DetailViewModel(
        getItemDetailsUseCase: KoinHelper.shared.getGetItemDetailsUseCase()
    )
    
    init() {
        startObserving()
    }
    
    deinit { vm.dispose() }
    
    private func startObserving() {
        FlowCollector.collect(flow: vm.details) { [weak self] (details: Any?) in
            self?.details = details as? ItemDetails
        }
    }
    
    func loadDetails(itemId: Int32, isMovie: Bool) {
        vm.loadDetails(id: itemId, isMovie: isMovie)
    }
}

struct DetailView: View {
    let item: Item
    @StateObject private var state = DetailViewModelState()
    @State private var isLoading = false
    @State private var showingPlayer = false
    @State private var isVideoLoading = true
    @State private var selectedSeason: Int32 = 1
    @State private var selectedEpisode: Int32 = 1
    @Environment(\.dismiss) private var dismiss
    
    private var backdropUrl: URL? {
        guard let path = state.details?.backdropPath ?? item.backdropPath else { return nil }
        return URL(string: "https://image.tmdb.org/t/p/w780\(path)")
    }
    
    private var posterUrl: URL? {
        guard let path = state.details?.posterPath ?? item.posterPath else { return nil }
        return URL(string: "https://image.tmdb.org/t/p/w342\(path)")
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Backdrop
                AsyncImage(url: backdropUrl) { phase in
                    switch phase {
                    case .success(let img):
                        img.resizable()
                            .aspectRatio(16/9, contentMode: .fill)
                            .clipped()
                    default:
                        Rectangle().fill(Color.gray.opacity(0.3))
                            .aspectRatio(16/9, contentMode: .fill)
                    }
                }
                .ignoresSafeArea(edges: .top)
                
                // Poster + Title row
                HStack(alignment: .top, spacing: 12) {
                    AsyncImage(url: posterUrl) { phase in
                        switch phase {
                        case .success(let img):
                            img.resizable()
                                .aspectRatio(2/3, contentMode: .fill)
                                .frame(width: 100, height: 150)
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                                .shadow(radius: 6)
                        default:
                            RoundedRectangle(cornerRadius: 10)
                                .fill(Color.gray.opacity(0.3))
                                .frame(width: 100, height: 150)
                        }
                    }
                    .offset(y: -30)
                    
                    VStack(alignment: .leading, spacing: 6) {
                        Text(state.details?.displayTitle ?? item.displayTitle)
                            .font(.title2)
                            .fontWeight(.bold)
                            .lineLimit(3)
                        
                        if let runtime = state.details?.runtime?.intValue, runtime > 0 {
                            Text("\(runtime) min")
                                .font(.subheadline)
                                .italic()
                                .foregroundColor(.secondary)
                        }
                        
                        HStack(spacing: 12) {
                            ratingBadge
                            if let year = displayYear { Text(year).font(.caption).foregroundColor(.secondary) }
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                
                // TV Show Pickers
                if !item.isMovie, let seasons = state.details?.seasons, !seasons.isEmpty {
                    HStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Season").font(.caption).foregroundColor(.secondary)
                            Picker("Season", selection: $selectedSeason) {
                                ForEach(seasons, id: \.seasonNumber) { season in
                                    Text("Season \(season.seasonNumber)").tag(season.seasonNumber)
                                }
                            }
                            .pickerStyle(.menu)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.gray.opacity(0.15))
                            .cornerRadius(8)
                            .onChange(of: selectedSeason) { _ in
                                selectedEpisode = 1
                            }
                        }
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Episode").font(.caption).foregroundColor(.secondary)
                            let currentSeason = seasons.first(where: { $0.seasonNumber == selectedSeason })
                            let maxEp = currentSeason?.episodeCount ?? 1
                            if maxEp > 0 {
                                Picker("Episode", selection: $selectedEpisode) {
                                    ForEach(1...maxEp, id: \.self) { ep in
                                        Text("Episode \(ep)").tag(Int32(ep))
                                    }
                                }
                                .pickerStyle(.menu)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.gray.opacity(0.15))
                                .cornerRadius(8)
                            }
                        }
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                }

                // Play Button
                Button(action: { showingPlayer = true }) {
                    HStack {
                        Image(systemName: "play.fill")
                        Text("Play Now")
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(LinearGradient(colors: [Color.purple, Color.blue], startPoint: .leading, endPoint: .trailing))
                    .cornerRadius(12)
                    .shadow(color: Color.purple.opacity(0.3), radius: 5, y: 3)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .fullScreenCover(isPresented: $showingPlayer) {
                    let streamPath = item.isMovie ? "/embed/movie/\(item.id)" : "/embed/tv/\(item.id)/\(selectedSeason)/\(selectedEpisode)"
                    if let url = URL(string: "https://streamimdb.ru" + streamPath) {
                        ZStack(alignment: .topLeading) {
                            MovieWebView(url: url, isLoading: $isVideoLoading)
                                .ignoresSafeArea()
                            
                            Button(action: { showingPlayer = false }) {
                                Image(systemName: "xmark")
                                    .font(.title3)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                    .padding(12)
                                    .background(.ultraThinMaterial)
                                    .clipShape(Circle())
                            }
                            .padding(.leading, 20)
                            .padding(.top, 16)
                            
                            if isVideoLoading {
                                ZStack {
                                    Color.black.ignoresSafeArea()
                                    VStack(spacing: 16) {
                                        ProgressView()
                                            .scaleEffect(1.5)
                                            .tint(.white)
                                        Text("Loading...")
                                            .font(.headline)
                                            .foregroundColor(.white)
                                    }
                                }
                            }
                        }
                        .onAppear {
                            isVideoLoading = true
                            DispatchQueue.main.asyncAfter(deadline: .now() + 4.0) {
                                isVideoLoading = false
                            }
                        }
                    }
                }
                
                // Genre chips
                if let genres = state.details?.genres, !genres.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(genres, id: \.id) { genre in
                                Text(genre.name)
                                    .font(.caption)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                                    .background(Color.red.opacity(0.15))
                                    .foregroundColor(.red)
                                    .clipShape(Capsule())
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    .padding(.vertical, 8)
                }
                
                // Overview
                if let overview = state.details?.overview ?? item.overview, !overview.isEmpty {
                    Text("Overview")
                        .font(.headline)
                        .padding(.horizontal, 16)
                        .padding(.top, 12)
                    
                    Text(overview)
                        .font(.body)
                        .foregroundColor(.secondary)
                        .padding(.horizontal, 16)
                        .padding(.top, 4)
                }
                
                // Cast
                if let cast = state.details?.credits?.cast, !cast.isEmpty {
                    Text("Cast")
                        .font(.headline)
                        .padding(.horizontal, 16)
                        .padding(.top, 16)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(cast.prefix(10), id: \.name) { member in
                                CastCard(cast: member)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                    }
                }
                
                // Trailers
                if let videos = state.details?.videos?.results?.filter({ $0.site == "YouTube" && $0.type == "Trailer" }), !videos.isEmpty {
                    Text("Trailers")
                        .font(.headline)
                        .padding(.horizontal, 16)
                        .padding(.top, 16)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(videos, id: \.key) { video in
                                if let url = URL(string: "https://www.youtube.com/watch?v=\(video.key)") {
                                    Link(destination: url) {
                                        ZStack {
                                            AsyncImage(url: URL(string: "https://img.youtube.com/vi/\(video.key)/hqdefault.jpg")) { image in
                                                image.resizable().aspectRatio(16/9, contentMode: .fill)
                                            } placeholder: {
                                                Rectangle().fill(Color.gray.opacity(0.3)).aspectRatio(16/9, contentMode: .fit)
                                            }
                                            .frame(width: 200, height: 112)
                                            .clipShape(RoundedRectangle(cornerRadius: 8))
                                            
                                            Image(systemName: "play.circle.fill")
                                                .font(.largeTitle)
                                                .foregroundColor(.white)
                                                .shadow(radius: 4)
                                        }
                                    }
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                    }
                }
                
                // Seasons (TV shows)
                if let seasons = state.details?.seasons, !seasons.isEmpty {
                    Text("Seasons")
                        .font(.headline)
                        .padding(.horizontal, 16)
                        .padding(.top, 16)
                    
                    ForEach(seasons, id: \.seasonNumber) { season in
                        HStack {
                            Text(season.name)
                                .font(.subheadline)
                            Spacer()
                            Text("\(season.episodeCount) episodes")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 6)
                        Divider().padding(.leading, 16)
                    }
                }
                
                Spacer(minLength: 32)
            }
        }
        .ignoresSafeArea(edges: .top)
        .navigationBarTitleDisplayMode(.inline)
        .task { await loadDetails() }
    }
    
    private var ratingBadge: some View {
        let rating = state.details?.voteAverage ?? item.voteAverage
        return HStack(spacing: 2) {
            Image(systemName: "star.fill").foregroundColor(.yellow).font(.caption)
            Text(String(format: "%.1f", rating)).font(.caption).fontWeight(.semibold)
        }
    }
    
    private var displayYear: String? {
        let dateStr = state.details?.displayDate ?? item.releaseDate ?? ""
        return String(dateStr.prefix(4)).isEmpty ? nil : String(dateStr.prefix(4))
    }
    
    @MainActor
    private func loadDetails() async {
        isLoading = true
        state.loadDetails(itemId: item.id, isMovie: item.isMovie)
        isLoading = false
    }
}

struct CastCard: View {
    let cast: Cast
    
    private var profileUrl: URL? {
        guard let path = cast.profilePath else { return nil }
        return URL(string: "https://image.tmdb.org/t/p/w185\(path)")
    }
    
    var body: some View {
        VStack(spacing: 4) {
            AsyncImage(url: profileUrl) { phase in
                switch phase {
                case .success(let img):
                    img.resizable().aspectRatio(2/3, contentMode: .fill)
                        .frame(width: 70, height: 100)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                default:
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.gray.opacity(0.3))
                        .frame(width: 70, height: 100)
                        .overlay(Image(systemName: "person.fill").foregroundColor(.gray))
                }
            }
            Text(cast.name)
                .font(.caption2)
                .lineLimit(2)
                .frame(width: 70)
                .multilineTextAlignment(.center)
        }
    }
}

// MARK: - Navigation conformance

extension Item: @retroactive Identifiable {}

import WebKit

// ── SafariPlayerView ──────────────────────────────────────────────────────
// Uses the real Safari engine (SFSafariViewController) which has full MSE/HLS
// video support. streamimdb.ru cannot detect it as a restricted WebView,
// so the working desktop-quality player is served consistently.
struct SafariPlayerView: UIViewControllerRepresentable {
    let url: URL
    let onDismiss: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let config = SFSafariViewController.Configuration()
        config.entersReaderIfAvailable = false
        config.barCollapsingEnabled = true

        let safari = SFSafariViewController(url: url, configuration: config)
        safari.preferredBarTintColor = .black
        safari.preferredControlTintColor = .white
        safari.dismissButtonStyle = .close
        safari.delegate = context.coordinator
        return safari
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    class Coordinator: NSObject, SFSafariViewControllerDelegate {
        let parent: SafariPlayerView
        init(_ parent: SafariPlayerView) { self.parent = parent }
        func safariViewControllerDidFinish(_ controller: SFSafariViewController) {
            parent.onDismiss()
        }
    }
}

struct MovieWebView: UIViewRepresentable {
    let url: URL
    @Binding var isLoading: Bool
    @Environment(\.presentationMode) var presentationMode
    
    func makeUIView(context: Context) -> WKWebView {
        let prefs = WKWebpagePreferences()
        prefs.allowsContentJavaScript = true
        
        let config = WKWebViewConfiguration()
        // FALSE = let iOS use native AVPlayer fullscreen for video (like YouTube/Netflix)
        // TRUE  = inline playback in the web view → black screen on many streams
        config.allowsInlineMediaPlayback = false
        config.mediaTypesRequiringUserActionForPlayback = []
        config.defaultWebpagePreferences = prefs
        
        // Ad-blocking JS: nuke common ad overlay elements and block popup windows
        let jsString = """
        // Remove common ad overlay elements
        setInterval(function() {
            ['[class*="ad-"]','[id*="ad-"]','[class*="popup"]','[id*="popup"]',
             '[class*="overlay"]','[class*="banner"]','iframe[src*="ad"]'].forEach(function(sel) {
                document.querySelectorAll(sel).forEach(function(el) {
                    if (!el.querySelector('video')) el.remove();
                });
            });
        }, 1000);
        """
        let script = WKUserScript(source: jsString, injectionTime: .atDocumentEnd, forMainFrameOnly: false)
        let userContentController = WKUserContentController()
        userContentController.addUserScript(script)
        config.userContentController = userContentController
        
        let webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = context.coordinator
        webView.uiDelegate = context.coordinator
        webView.isOpaque = false                              // ← transparent during load = no white flash
        webView.backgroundColor = .black
        webView.scrollView.backgroundColor = .black
        webView.scrollView.contentInsetAdjustmentBehavior = .never  // ← prevents layout jumps after video dismiss
        
        // Spoof as Android Chrome — streamimdb.ru detects iOS Safari and serves broken player
        webView.customUserAgent = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        
        var request = URLRequest(url: url)
        request.setValue("https://streamimdb.ru/", forHTTPHeaderField: "Referer")
        webView.load(request)
        
        // Fallback: hide loading screen after 5 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 5.0) {
            self.isLoading = false
        }
        
        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        context.coordinator.parent = self
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        var parent: MovieWebView
        
        let adHosts: Set<String> = [
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adservice.google.com", "adservice.google.co.in",
            "pagead2.googlesyndication.com", "tpc.googlesyndication.com",
            "ads.pubmatic.com", "simage2.pubmatic.com",
            "secure.adnxs.com", "ib.adnxs.com",
            "prebid.io", "prebid.org",
            "taboola.com", "trc.taboola.com",
            "outbrain.com", "widgets.outbrain.com",
            "amazon-adsystem.com", "aax.amazon-adsystem.com",
            "criteo.com", "static.criteo.net",
            "advertising.com", "adtech.com",
            "rubiconproject.com", "ads.rubiconproject.com",
            "openx.net", "openx.com",
            "moatads.com", "z.moatads.com",
            "casalemedia.com", "scdn.cxense.com",
            "ads.yahoo.com", "media.net",
            "scorecardresearch.com",
            "cdn.admanager.com", "ads.exoclick.com", "adx.ads.exoclick.com",
            "popads.net", "popcash.net", "trafficjunky.net",
            "traffichaus.com", "trafficfactory.biz",
            "propellerads.com", "admaven.com",
            "revcontent.com", "adtelligent.com"
        ]
        
        init(_ parent: MovieWebView) {
            self.parent = parent
        }
        
        private func shouldBlock(url: URL?) -> Bool {
            guard let host = url?.host?.lowercased() else { return false }
            return adHosts.contains { host == $0 || host.hasSuffix(".\($0)") }
        }
        
        // Block navigation to ad hosts
        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            if shouldBlock(url: navigationAction.request.url) {
                decisionHandler(.cancel)
                return
            }
            
            // Block external link activations (ad pop-ups), allow everything else
            if navigationAction.navigationType == .linkActivated {
                let host = navigationAction.request.url?.host ?? ""
                if !host.contains("lhr.life") && !host.contains("streamimdb.ru") {
                    decisionHandler(.cancel)
                    return
                }
            }
            
            decisionHandler(.allow)
        }
        
        // Prevent popup windows (extremely common on streaming sites)
        func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
            // Never open new tabs/windows for popups
            return nil
        }
        
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            DispatchQueue.main.async {
                self.parent.isLoading = false
            }
        }
        
        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            DispatchQueue.main.async {
                self.parent.isLoading = false
            }
        }
        
        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
            DispatchQueue.main.async {
                self.parent.isLoading = false
            }
        }
    }
}

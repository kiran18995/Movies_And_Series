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
                    if let url = URL(string: item.isMovie ? "https://streamimdb.ru/embed/movie/\(item.id)" : "https://streamimdb.ru/embed/tv/\(item.id)/\(selectedSeason)/\(selectedEpisode)") {
                        SafariView(url: url)
                            .ignoresSafeArea()
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

struct SafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        return SFSafariViewController(url: url)
    }

    func updateUIViewController(_ uiViewController: SFSafariViewController, context: Context) {
    }
}

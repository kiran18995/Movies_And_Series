import SwiftUI
import shared

struct TvShowsView: View {
    @State private var tvShows: [Item] = []
    @State private var isLoading = false
    @State private var selectedItem: Item? = nil
    
    private let useCase: GetTvShowsListUseCase = KoinHelper.shared.getGetTvShowsListUseCase()
    
    var body: some View {
        NavigationStack {
            Group {
                if isLoading && tvShows.isEmpty {
                    ProgressView("Loading…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    tvShowsGrid
                }
            }
            .navigationTitle("TV Shows")
            .navigationDestination(item: $selectedItem) { item in
                DetailView(item: item)
            }
            .onAppear { Task { await loadTvShows() } }
        }
    }
    
    private var tvShowsGrid: some View {
        ScrollView {
            LazyVGrid(
                columns: [GridItem(.adaptive(minimum: 110), spacing: 12)],
                spacing: 16
            ) {
                ForEach(tvShows, id: \.id) { item in
                    PosterCard(item: item)
                        .onTapGesture { selectedItem = item }
                }
            }
            .padding(16)
        }
        .refreshable { await loadTvShows() }
    }
    
    @MainActor
    private func loadTvShows() async {
        // Placeholder - actual loading is done through the KMM ViewModel
    }
}

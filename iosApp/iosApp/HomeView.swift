import SwiftUI
import shared

// MARK: - Lightweight observable wrapper

final class HomeViewModelState: ObservableObject {
    @Published var movies: [Item] = []
    @Published var tvShows: [Item] = []
    @Published var upcoming: [Item] = []
    @Published var isLoading: Bool = false
    
    private let vm: HomeViewModel
    
    init() {
        let moviesUseCase = KoinHelper.shared.getGetMoviesListUseCase()
        let tvUseCase = KoinHelper.shared.getGetTvShowsListUseCase()
        let upcomingUseCase = KoinHelper.shared.getGetUpcomingMoviesUseCase()
        self.vm = HomeViewModel(
            getMoviesListUseCase: moviesUseCase,
            getTvShowsListUseCase: tvUseCase,
            getUpcomingMoviesUseCase: upcomingUseCase
        )
        startObserving()
    }
    
    deinit { vm.dispose() }
    
    private func startObserving() {
        FlowCollector.collect(flow: vm.movies) { [weak self] (items: Any) in
            self?.movies = items as! [Item]
        }
        FlowCollector.collect(flow: vm.tvShows) { [weak self] (items: Any) in
            self?.tvShows = items as! [Item]
        }
        FlowCollector.collect(flow: vm.upcomingMovies) { [weak self] (items: Any) in
            self?.upcoming = items as! [Item]
        }
        FlowCollector.collect(flow: vm.isLoading) { [weak self] (loading: Any) in
            self?.isLoading = loading as! Bool
        }
    }
    
    func refresh() { vm.load() }
}

// MARK: - Home View

struct HomeView: View {
    @StateObject private var state = HomeViewModelState()
    @State private var selectedItem: Item? = nil
    
    var body: some View {
        NavigationStack {
            Group {
                if state.isLoading && state.movies.isEmpty {
                    ProgressView("Loading…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 0) {
                            if !state.upcoming.isEmpty {
                                sectionHeader("Upcoming Movies")
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 12) {
                                        ForEach(state.upcoming, id: \.id) { item in
                                            PosterCard(item: item)
                                                .onTapGesture { selectedItem = item }
                                        }
                                    }
                                    .padding(.horizontal, 16)
                                }
                                .padding(.bottom, 16)
                            }
                            
                            sectionHeader("Popular Movies")
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(state.movies, id: \.id) { item in
                                        PosterCard(item: item)
                                            .onTapGesture { selectedItem = item }
                                    }
                                }
                                .padding(.horizontal, 16)
                            }
                            .padding(.bottom, 16)
                            
                            sectionHeader("Popular TV Shows")
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(state.tvShows, id: \.id) { item in
                                        PosterCard(item: item)
                                            .onTapGesture { selectedItem = item }
                                    }
                                }
                                .padding(.horizontal, 16)
                            }
                            .padding(.bottom, 24)
                        }
                    }
                    .refreshable { state.refresh() }
                }
            }
            .navigationTitle("Discover")
            .navigationDestination(item: $selectedItem) { item in
                DetailView(item: item)
            }
        }
    }
    
    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.headline)
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)
    }
}

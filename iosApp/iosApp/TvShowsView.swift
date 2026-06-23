import SwiftUI
import shared

enum TvCategory: String, CaseIterable {
    case popular = "popular"
    case topRated = "top_rated"
    case airingToday = "airing_today"
    case onTheAir = "on_the_air"

    var displayName: String {
        switch self {
        case .popular: return "Popular"
        case .topRated: return "Top Rated"
        case .airingToday: return "Airing Today"
        case .onTheAir: return "On The Air"
        }
    }
    
    var endpoint: String { self.rawValue }
}

struct TvShowsView: View {
    @State private var tvShows: [Item] = []
    @State private var isLoading = false
    @State private var selectedItem: Item? = nil
    
    @State private var currentPage: Int32 = 1
    @State private var isLoadingMore = false
    
    // Category Filter
    @State private var selectedCategory: TvCategory = .popular
    
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
            .onAppear { Task { await loadTvShows(reset: true) } }
            .onChange(of: selectedCategory) { _ in
                Task { await loadTvShows(reset: true) }
            }
        }
    }
    
    private var tvShowsGrid: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Category Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(TvCategory.allCases, id: \.self) { category in
                            filterChip(
                                title: category.displayName,
                                isSelected: selectedCategory == category
                            ) {
                                selectedCategory = category
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
                .padding(.top, 8)
                .padding(.bottom, 16)
                
                LazyVGrid(
                    columns: [GridItem(.adaptive(minimum: 110), spacing: 12)],
                    spacing: 16
                ) {
                    ForEach(tvShows, id: \.id) { item in
                        PosterCard(item: item)
                            .onTapGesture { selectedItem = item }
                            .onAppear {
                                if item.id == tvShows.last?.id {
                                    Task { await loadTvShows() }
                                }
                            }
                    }
                    
                    if isLoadingMore {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 80)
            }
        }
        .refreshable { await loadTvShows(reset: true) }
    }
    
    private func filterChip(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.footnote)
                .fontWeight(isSelected ? .semibold : .regular)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color(UIColor.systemGray5))
                .foregroundColor(isSelected ? .white : .primary)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
    
    @MainActor
    private func loadTvShows(reset: Bool = false) async {
        if reset {
            currentPage = 1
            if tvShows.isEmpty { isLoading = true }
        } else {
            guard !isLoading && !isLoadingMore else { return }
            isLoadingMore = true
            currentPage += 1
        }
        
        do {
            let newItems = try await useCase.invoke(
                category: selectedCategory.endpoint,
                page: currentPage,
                query: ""
            )
            if reset {
                self.tvShows = newItems
            } else {
                self.tvShows.append(contentsOf: newItems)
            }
        } catch {
            print("Failed to load TV shows: \(error)")
        }
        
        isLoading = false
        isLoadingMore = false
    }
}

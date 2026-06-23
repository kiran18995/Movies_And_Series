import SwiftUI
import shared

enum MovieLanguage: String, CaseIterable {
    case english = "en"
    case hindi = "hi"
    case kannada = "kn"
    case telugu = "te"
    case tamil = "ta"
    case malayalam = "ml"

    var displayName: String {
        switch self {
        case .english: return "English"
        case .hindi: return "Hindi"
        case .kannada: return "Kannada"
        case .telugu: return "Telugu"
        case .tamil: return "Tamil"
        case .malayalam: return "Malayalam"
        }
    }
    
    var code: String { self.rawValue }
}

enum MovieSortOrder: String, CaseIterable {
    case popular = "popularity.desc"
    case topRated = "vote_average.desc"
    case newToOld = "primary_release_date.desc"
    case oldToNew = "primary_release_date.asc"

    var displayName: String {
        switch self {
        case .popular: return "Popular"
        case .topRated: return "Top Rated"
        case .newToOld: return "New to Old"
        case .oldToNew: return "Old to New"
        }
    }
    
    var value: String { self.rawValue }
}

// MARK: - Home View (Movies)

struct HomeView: View {
    @State private var movies: [Item] = []
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @State private var selectedItem: Item? = nil
    
    @State private var currentPage: Int32 = 1
    @State private var isLoadingMore = false
    
    // Filters
    @State private var selectedLanguage: MovieLanguage = .english
    @State private var selectedSortOrder: MovieSortOrder = .popular
    
    private let useCase: DiscoverMoviesListUseCase = KoinHelper.shared.getDiscoverMoviesListUseCase()
    
    var body: some View {
        NavigationStack {
            Group {
                if let errorMessage = errorMessage {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 48))
                            .foregroundColor(.red)
                        Text("Error Loading Movies")
                            .font(.headline)
                        Text(errorMessage)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                        Button("Retry") {
                            Task { await loadMovies(reset: true) }
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if isLoading && movies.isEmpty {
                    ProgressView("Loading…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    moviesGrid
                }
            }
            .navigationTitle("Movies")
            .navigationDestination(item: $selectedItem) { item in
                DetailView(item: item)
            }
            .onAppear { Task { await loadMovies(reset: true) } }
            .onChange(of: selectedLanguage) { _ in
                Task { await loadMovies(reset: true) }
            }
            .onChange(of: selectedSortOrder) { _ in
                Task { await loadMovies(reset: true) }
            }
        }
    }
    
    private var moviesGrid: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Filters Header
                filterSection
                    .padding(.bottom, 16)
                
                LazyVGrid(
                    columns: [GridItem(.adaptive(minimum: 110), spacing: 12)],
                    spacing: 16
                ) {
                    ForEach(movies, id: \.id) { item in
                        PosterCard(item: item)
                            .onTapGesture { selectedItem = item }
                            .onAppear {
                                if item.id == movies.last?.id {
                                    Task { await loadMovies() }
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
    }
    
    private var filterSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Language Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(MovieLanguage.allCases, id: \.self) { language in
                        filterChip(
                            title: language.displayName,
                            isSelected: selectedLanguage == language,
                            selectedColor: .red
                        ) {
                            selectedLanguage = language
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
            
            // Sort Order Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(MovieSortOrder.allCases, id: \.self) { sort in
                        filterChip(
                            title: sort.displayName,
                            isSelected: selectedSortOrder == sort,
                            selectedColor: .gray
                        ) {
                            selectedSortOrder = sort
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .padding(.top, 8)
    }
    
    private func filterChip(title: String, isSelected: Bool, selectedColor: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.footnote)
                .fontWeight(isSelected ? .semibold : .regular)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? selectedColor : Color(UIColor.systemGray5))
                .foregroundColor(isSelected ? .white : .primary)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
    
    @MainActor
    private func loadMovies(reset: Bool = false) async {
        if reset {
            currentPage = 1
            if movies.isEmpty { isLoading = true }
        } else {
            guard !isLoading && !isLoadingMore else { return }
            isLoadingMore = true
            currentPage += 1
        }
        
        do {
            let newItems = try await useCase.invoke(
                language: selectedLanguage.code,
                sortBy: selectedSortOrder.value,
                page: currentPage,
                query: ""
            )
            if reset {
                self.movies = newItems
            } else {
                self.movies.append(contentsOf: newItems)
            }
            self.errorMessage = nil
        } catch {
            print("Failed to load movies: \(error)")
            self.errorMessage = "\(error)"
        }
        
        isLoading = false
        isLoadingMore = false
    }
}

import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Movies", systemImage: "film")
                }
            
            TvShowsView()
                .tabItem {
                    Label("TV Shows", systemImage: "tv")
                }
            
            SavedView()
                .tabItem {
                    Label("Saved", systemImage: "bookmark.fill")
                }
            
            SearchView()
                .tabItem {
                    Label("Search", systemImage: "magnifyingglass")
                }
        }
        .accentColor(.red)
    }
}

import shared

struct SearchView: View {
    @State private var searchQuery = ""
    @State private var items: [Item] = []
    @State private var isLoading = false
    @State private var selectedItem: Item? = nil
    @State private var searchType: SearchType = .movies
    
    private let movieUseCase: GetMoviesListUseCase = KoinHelper.shared.getGetMoviesListUseCase()
    private let tvUseCase: GetTvShowsListUseCase = KoinHelper.shared.getGetTvShowsListUseCase()
    
    enum SearchType: String, CaseIterable {
        case movies = "Movies"
        case tvShows = "TV Shows"
    }
    
    var body: some View {
        NavigationStack {
            Group {
                if isLoading && items.isEmpty {
                    ProgressView("Searching…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if items.isEmpty && !searchQuery.isEmpty && !isLoading {
                    Text("No results found for \"\(searchQuery)\"")
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if items.isEmpty {
                    VStack(spacing: 16) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 64))
                            .foregroundColor(.gray.opacity(0.5))
                        Text("Find movies and TV shows")
                            .font(.headline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVGrid(
                            columns: [GridItem(.adaptive(minimum: 110), spacing: 12)],
                            spacing: 16
                        ) {
                            ForEach(items, id: \.id) { item in
                                PosterCard(item: item)
                                    .onTapGesture { selectedItem = item }
                            }
                        }
                        .padding(16)
                    }
                }
            }
            .navigationTitle("Search")
            .navigationDestination(item: $selectedItem) { item in
                DetailView(item: item)
            }
            .searchable(text: $searchQuery, prompt: "Search...")
            .searchScopes($searchType) {
                ForEach(SearchType.allCases, id: \.self) { type in
                    Text(type.rawValue).tag(type)
                }
            }
            .onChange(of: searchQuery) { _ in
                Task { await performSearch() }
            }
            .onChange(of: searchType) { _ in
                Task { await performSearch() }
            }
        }
    }
    
    @MainActor
    private func performSearch() async {
        guard !searchQuery.trimmingCharacters(in: .whitespaces).isEmpty else {
            items = []
            return
        }
        
        isLoading = true
        do {
            if searchType == .movies {
                let results = try await movieUseCase.invoke(category: "popular", page: 1, query: searchQuery)
                self.items = results
            } else {
                let results = try await tvUseCase.invoke(category: "popular", page: 1, query: searchQuery)
                self.items = results
            }
        } catch {
            print("Search failed: \(error)")
        }
        isLoading = false
    }
}

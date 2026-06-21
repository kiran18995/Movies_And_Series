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
        }
        .accentColor(.red)
    }
}

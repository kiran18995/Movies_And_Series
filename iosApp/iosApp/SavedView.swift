import SwiftUI
import shared

final class SavedViewModelState: ObservableObject {
    @Published var bookmarks: [Item] = []
    private let vm = SavedViewModel(
        getAllBookmarksUseCase: KoinHelper.shared.getGetAllBookmarksUseCase(),
        toggleBookmarkUseCase: KoinHelper.shared.getToggleBookmarkUseCase()
    )

    init() {
        startObserving()
    }

    deinit {
        vm.dispose()
    }

    private func startObserving() {
        FlowCollector.collect(flow: vm.bookmarks) { [weak self] (items: Any) in
            self?.bookmarks = items as! [Item]
        }
    }
}

struct SavedView: View {
    @StateObject private var state = SavedViewModelState()
    @State private var selectedItem: Item? = nil
    
    var body: some View {
        NavigationStack {
            Group {
                if state.bookmarks.isEmpty {
                    VStack(spacing: 16) {
                        Image(systemName: "bookmark.slash")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary)
                        Text("No saved items yet")
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List(state.bookmarks, id: \.id) { item in
                        BookmarkRow(item: item)
                            .onTapGesture { selectedItem = item }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Saved")
            .navigationDestination(item: $selectedItem) { item in
                DetailView(item: item)
            }
        }
    }
}

struct BookmarkRow: View {
    let item: Item
    
    private var posterUrl: URL? {
        guard let path = item.posterPath else { return nil }
        return URL(string: "https://image.tmdb.org/t/p/w92\(path)")
    }
    
    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: posterUrl) { phase in
                switch phase {
                case .success(let img):
                    img.resizable().aspectRatio(2/3, contentMode: .fill)
                        .frame(width: 50, height: 75)
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                default:
                    RoundedRectangle(cornerRadius: 6)
                        .fill(Color.gray.opacity(0.3))
                        .frame(width: 50, height: 75)
                }
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.displayTitle)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .lineLimit(2)
                
                HStack(spacing: 4) {
                    Image(systemName: "star.fill")
                        .foregroundColor(.yellow)
                        .font(.caption2)
                    Text(String(format: "%.1f", item.voteAverage))
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    Text(item.isMovie ? "Movie" : "TV Show")
                        .font(.caption2)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(item.isMovie ? Color.blue.opacity(0.2) : Color.purple.opacity(0.2))
                        .cornerRadius(4)
                }
            }
        }
        .padding(.vertical, 4)
    }
}

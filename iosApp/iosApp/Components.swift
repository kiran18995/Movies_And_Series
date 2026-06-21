import SwiftUI
import shared

// MARK: - Poster Card

struct PosterCard: View {
    let item: Item
    private var posterUrl: URL? {
        guard let path = item.posterPath else { return nil }
        return URL(string: "https://image.tmdb.org/t/p/w342\(path)")
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            AsyncImage(url: posterUrl) { phase in
                switch phase {
                case .empty:
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.gray.opacity(0.3))
                        .overlay(ProgressView())
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(2/3, contentMode: .fill)
                        .clipped()
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                case .failure:
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.gray.opacity(0.2))
                        .overlay(Image(systemName: "photo").foregroundColor(.gray))
                @unknown default:
                    EmptyView()
                }
            }
            .frame(width: 120, height: 180)
            
            Text(item.displayTitle)
                .font(.caption)
                .fontWeight(.medium)
                .lineLimit(2)
                .frame(width: 120, alignment: .leading)
            
            HStack(spacing: 2) {
                Image(systemName: "star.fill")
                    .foregroundColor(.yellow)
                    .font(.caption2)
                Text(String(format: "%.1f", item.voteAverage))
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
        }
    }
}

// MARK: - Backdrop Card

struct BackdropCard: View {
    let item: Item
    private var backdropUrl: URL? {
        guard let path = item.backdropPath else { return nil }
        return URL(string: "https://image.tmdb.org/t/p/w780\(path)")
    }
    
    var body: some View {
        ZStack(alignment: .bottomLeading) {
            AsyncImage(url: backdropUrl) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(16/9, contentMode: .fill)
                        .clipped()
                case .empty:
                    Rectangle().fill(Color.gray.opacity(0.3)).overlay(ProgressView())
                default:
                    Rectangle().fill(Color.gray.opacity(0.2))
                }
            }
            .frame(width: 280, height: 157)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            
            LinearGradient(
                gradient: Gradient(colors: [.black.opacity(0.7), .clear]),
                startPoint: .bottom,
                endPoint: .center
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .frame(width: 280, height: 157)
            
            Text(item.displayTitle)
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundColor(.white)
                .padding(8)
        }
        .frame(width: 280, height: 157)
    }
}

// Swift uses Kotlin's KoinHelper

// MARK: - Flow Collector Helper

enum FlowCollector {
    static func collect<T>(flow: any AnyObject, handler: @escaping (T) -> Void) {
        guard let stateFlow = flow as? Kotlinx_coroutines_coreStateFlow else { return }
        
        // Use the KMP exposed FlowHelper to collect the StateFlow
        _ = FlowHelper.shared.collectStateFlow(flow: stateFlow) { value in
            if let v = value as? T {
                handler(v)
            }
        }
    }
}

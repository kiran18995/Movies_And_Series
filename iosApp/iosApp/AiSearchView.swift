import SwiftUI
import shared
import AVFoundation

struct AiSearchView: View {
    @StateObject private var viewModelWrapper = AiSearchViewModelWrapper()
    @State private var selectedItem: Item? = nil
    
    var body: some View {
        NavigationStack {
            VStack {
                Spacer().frame(height: 20)
                
                Text("Tap the mic and say something like 'Show me funny action movies from 2015'")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                
                Spacer().frame(height: 30)
                
                Button(action: {
                    if viewModelWrapper.isListening {
                        viewModelWrapper.stopListening()
                        viewModelWrapper.processVoiceQuery(query: viewModelWrapper.speechText)
                    } else {
                        viewModelWrapper.startListening()
                    }
                }) {
                    ZStack {
                        Circle()
                            .fill(viewModelWrapper.isListening ? Color.red : Color.blue.opacity(0.2))
                            .frame(width: 100, height: 100)
                        
                        Image(systemName: viewModelWrapper.isListening ? "stop.fill" : "mic.fill")
                            .font(.system(size: 40))
                            .foregroundColor(viewModelWrapper.isListening ? .white : .blue)
                    }
                }
                
                Spacer().frame(height: 20)
                
                if viewModelWrapper.isListening {
                    Text("Listening: \(viewModelWrapper.speechText)")
                        .font(.body)
                } else if !viewModelWrapper.speechError.isEmpty {
                    Text("Error: \(viewModelWrapper.speechError)")
                        .foregroundColor(.red)
                } else if !viewModelWrapper.speechText.isEmpty {
                    Text("You said: \(viewModelWrapper.speechText)")
                        .font(.body)
                }
                
                if !viewModelWrapper.aiInterpretation.isEmpty {
                    Text(viewModelWrapper.aiInterpretation)
                        .font(.caption)
                        .foregroundColor(.purple)
                        .padding(.top, 8)
                }
                
                Spacer().frame(height: 20)
                
                if viewModelWrapper.isLoading {
                    ProgressView()
                } else {
                    ScrollView {
                        LazyVGrid(
                            columns: [GridItem(.adaptive(minimum: 110), spacing: 12)],
                            spacing: 16
                        ) {
                            ForEach(viewModelWrapper.results, id: \.id) { item in
                                PosterCard(item: item)
                                    .onTapGesture { selectedItem = item }
                            }
                        }
                        .padding(16)
                    }
                }
                
                Spacer()
            }
            .navigationTitle("AI Voice Search")
            .navigationDestination(item: $selectedItem) { item in
                DetailView(item: item)
            }
        }
    }
}

class AiSearchViewModelWrapper: ObservableObject {
    private let viewModel: SharedAiSearchViewModel
    private let speechRecognizer = IosSpeechRecognizer()
    
    @Published var isListening: Bool = false
    @Published var speechText: String = ""
    @Published var speechError: String = ""
    @Published var results: [Item] = []
    @Published var isLoading: Bool = false
    @Published var aiInterpretation: String = ""
    
    private var stateJob: CloseableJob?
    private var resultsJob: CloseableJob?
    private var loadingJob: CloseableJob?
    private var interpJob: CloseableJob?
    
    init() {
        // Need to create the viewModel natively or get it from Koin with the speechRecognizer
        // Let's get it from Koin Helper but we need to inject our local speechRecognizer!
        // Actually, since IosKoinSetup doesn't know about IosSpeechRecognizer, we can't easily get it via Koin unless we modify Koin graph.
        // Wait! In SharedAiSearchViewModel, SpeechRecognizer is passed in the constructor.
        // Koin is used in iOS to get SharedAiSearchViewModel, BUT the factory in IosKoinSetup will crash if it can't find SpeechRecognizer!
        
        let koinApp = KoinHelper.shared
        // Let's just create it manually since it's simple enough, or we can use Koin if we registered it.
        // Wait, we didn't register SpeechRecognizer in IosKoinSetup.kt!
        self.viewModel = SharedAiSearchViewModel(
            speechRecognizer: speechRecognizer,
            aiSearchUseCase: koinApp.getAiSearchUseCase(), // Wait, getAiSearchUseCase doesn't exist in KoinHelper!
            discoverMoviesListUseCase: koinApp.getDiscoverMoviesListUseCase(),
            getMoviesListUseCase: koinApp.getGetMoviesListUseCase(),
            getTvShowsListUseCase: koinApp.getGetTvShowsListUseCase()
        )
        
        stateJob = FlowHelper.shared.collectStateFlow(flow: viewModel.speechState) { [weak self] state in
            guard let self = self, let speechState = state as? SpeechState else { return }
            DispatchQueue.main.async {
                self.isListening = speechState.isListening
                self.speechText = speechState.text
                self.speechError = speechState.error ?? ""
            }
        }
        
        resultsJob = FlowHelper.shared.collectStateFlow(flow: viewModel.results) { [weak self] res in
            guard let self = self, let items = res as? [Item] else { return }
            DispatchQueue.main.async {
                self.results = items
            }
        }
        
        loadingJob = FlowHelper.shared.collectStateFlow(flow: viewModel.isLoading) { [weak self] loading in
            guard let self = self, let load = loading as? Bool else { return }
            DispatchQueue.main.async {
                self.isLoading = load
            }
        }
        
        interpJob = FlowHelper.shared.collectStateFlow(flow: viewModel.aiInterpretation) { [weak self] interp in
            guard let self = self, let interpretation = interp as? String else { return }
            DispatchQueue.main.async {
                self.aiInterpretation = interpretation
            }
        }
    }
    
    func startListening() {
        viewModel.startListening()
    }
    
    func stopListening() {
        viewModel.stopListening()
    }
    
    func processVoiceQuery(query: String) {
        viewModel.processVoiceQuery(query: query)
    }
    
    deinit {
        stateJob?.cancel()
        resultsJob?.cancel()
        loadingJob?.cancel()
        interpJob?.cancel()
        viewModel.dispose()
    }
}

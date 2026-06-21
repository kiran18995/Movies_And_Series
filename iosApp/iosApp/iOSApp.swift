import SwiftUI
import shared

@main
struct iOSApp: App {
    
    init() {
        // Initialise the shared KMM Koin container
        let db = IosRoomDatabaseFactoryKt.createIosRoomDatabase()
        
        // Load API token from Config.plist
        let apiToken = Bundle.main.infoDictionary?["TMDB_API_TOKEN"] as? String ?? ""
        
        IosKoinSetupKt.doInitKoin(apiToken: apiToken, database: db)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

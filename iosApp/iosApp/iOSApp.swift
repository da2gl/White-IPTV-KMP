import BackgroundTasks
import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    init() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.simplevideo.whiteiptv.playlistRefresh",
            using: nil
        ) { task in
            let scheduler = KoinHelper.shared.getIOSBackgroundScheduler()
            scheduler.handleBackgroundTask(task: task)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

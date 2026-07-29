//
//  vetnutriApp.swift
//  vetnutri
//
//  Created by Sebastien Lefebvre on 29/08/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

@main
struct vetnutriApp: App {
    let persistenceController = PersistenceController.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.managedObjectContext, persistenceController.container.viewContext)
        }
    }
}

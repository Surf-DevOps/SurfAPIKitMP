// swift-tools-version: 5.9
import PackageDescription

// Atualizado automaticamente pelo workflow .github/workflows/release.yml a cada tag.
let package = Package(
    name: "SurfAPIKit",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "SurfAPIKit", targets: ["SurfAPIKit"])
    ],
    targets: [
        .binaryTarget(
            name: "SurfAPIKit",
            url: "https://github.com/Surf-DevOps/SurfAPIKitMP/releases/download/v1.0.0/SurfAPIKit.xcframework.zip",
            checksum: "a4abe5a0e754e7a49b8fb9af9a7793c5306baf504fb20231df146619c5de93be"
        )
    ]
)

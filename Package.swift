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
            url: "https://github.com/Surf-DevOps/SurfAPIKitMP/releases/download/v1.0.5/SurfAPIKit.xcframework.zip",
            checksum: "85437e16fd038330d86f1bcee81c77bed3747cdf3a6a900e344265bde8b3349f"
        )
    ]
)

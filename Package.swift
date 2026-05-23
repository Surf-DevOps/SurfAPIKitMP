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
            url: "https://github.com/Surf-DevOps/SurfAPIKitMP/releases/download/v1.0.7/SurfAPIKit.xcframework.zip",
            checksum: "06bb283c6f593ead21c0851def2935ad4d49a6018aead699ce8935dd95e5d05e"
        )
    ]
)

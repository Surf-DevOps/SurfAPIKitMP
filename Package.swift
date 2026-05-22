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
            url: "https://github.com/Surf-DevOps/SurfAPIKitMP/releases/download/v1.0.3/SurfAPIKit.xcframework.zip",
            checksum: "8303b7cd9008b4e9b940a3e0e4595f224783ab93174b70886871883b1e61d1da"
        )
    ]
)

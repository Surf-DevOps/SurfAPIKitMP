// swift-tools-version: 5.9
import PackageDescription

// IMPORTANTE: este Package.swift é atualizado automaticamente pelo workflow .github/workflows/release.yml
// a cada nova tag — a URL e o checksum apontam para o XCFramework anexado na GitHub Release correspondente.
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
            checksum: "REPLACE_ME_ON_FIRST_RELEASE"
        )
    ]
)

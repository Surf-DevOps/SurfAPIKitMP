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
            url: "https://github.com/Surf-DevOps/SurfAPIKitMP/releases/download/v1.0.1/SurfAPIKit.xcframework.zip",
            checksum: "946ff855c8920d68f5a4edcfaac6decf8b8f310aa35ed9ba4e756a3842349a00"
        )
    ]
)

// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "ConvosMetrics",
    platforms: [
        .iOS(.v14),
        .macOS(.v11),
    ],
    products: [
        .library(name: "ConvosMetrics", targets: ["ConvosMetrics"]),
    ],
    targets: [
        .target(name: "ConvosMetrics", path: "ConvosMetrics/Sources/ConvosMetrics"),
    ]
)

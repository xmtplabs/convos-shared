import CryptoKit
import Foundation

public struct MetricsStableIdEncoder {
    public let salt: Data
    public let info: Data

    public init(salt: Data, info: Data) {
        self.salt = salt
        self.info = info
    }

    public func derive(privateKey: Data) -> String {
        let key = HKDF<SHA256>.deriveKey(
            inputKeyMaterial: SymmetricKey(data: privateKey),
            salt: salt,
            info: info,
            outputByteCount: 32
        )
        return key.withUnsafeBytes { Data($0) }
            .map { String(format: "%02x", $0) }
            .joined()
    }
}

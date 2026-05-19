# SurfAPIKitMP

SDK Kotlin Multiplatform da API Surf — mesmo código, mesmas funções, **Android e iOS**.

Sem configurar nada no app: adicione a dependência e chame.

---

## Android (JitPack)

`settings.gradle.kts` do projeto:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

`build.gradle.kts` do módulo do app:

```kotlin
dependencies {
    implementation("com.github.Surf-DevOps:SurfAPIKitMP:1.0.0")
}
```

Permissões já vêm declaradas no `AndroidManifest.xml` da lib (INTERNET, ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION). O `Context` é capturado automaticamente via `androidx.startup`.

## iOS (Swift Package Manager)

Xcode → File → Add Packages → cole:

```
https://github.com/Surf-DevOps/SurfAPIKitMP.git
```

E `import SurfAPIKit`.

---

## Uso

### Login + auth automática

```kotlin
// Kotlin (Android)
import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.features.authentication.login.*

val res = SurfApiKit.login(LoginRequest(nuDocumento = "...", dsPassword = "...", coMvno = 1))
```

```swift
// Swift (iOS) — via SKIE
import SurfAPIKit

let res = try await SurfApiKit.shared.login(
    request: LoginRequest(nuDocumento: "...", dsPassword: "...", coMvno: 1)
)
```

O `accessToken`/`refreshToken` são salvos automaticamente (Keychain no iOS, EncryptedSharedPreferences no Android) e injetados como `Authorization: Bearer ...` em todas as próximas chamadas. **Refresh em 401 é automático** via plugin Auth do Ktor — você não precisa orquestrar nada.

### Trocar ambiente (opcional)

Por padrão usa produção. Pra staging:

```kotlin
SurfApiKit.configure(environment = ApiEnvironment.STAGING)
```

```swift
SurfApiKit.shared.configure(environment: ApiEnvironment.companion.STAGING, tokenStore: nil)
```

### LocationManager (DDD por GPS)

```kotlin
val result = LocationManager().requestDDD()
when (result) {
    is DDDResult.Found -> println("DDD: ${result.ddd}")
    DDDResult.NotFound -> /* alert: digite seu DDD */
    DDDResult.PermissionDenied -> /* alert: ative permissão */
    DDDResult.LocationUnavailable -> /* alert: ative localização */
}
```

### ViaCEP

```kotlin
val resp = ViaCEPClient.buscarCEP("01310-100")
println(resp.localidade) // São Paulo
```

---

## Tratamento de erros

Todas as chamadas suspend lançam `ApiError`:

```kotlin
try {
    val res = SurfApiKit.login(req)
} catch (e: ApiError.Api) {
    // Erro estruturado da API (e.code, e.apiMessage)
} catch (e: ApiError.Server) {
    // HTTP 4xx/5xx
} catch (e: ApiError.Transport) {
    // Rede
} catch (e: ApiError.Decoding) {
    // Parsing
}
```

Em todos eles, `e.userDisplayMessage` traz uma string pronta pra alert ("Erro 42\nDescrição da API").

---

## Como release funciona

1. Crie uma tag `vX.Y.Z` no GitHub.
2. **JitPack** detecta a tag e compila o AAR sozinho. Android pronto na hora.
3. **GitHub Actions** (`.github/workflows/release.yml`) roda em runner macOS, gera o `SurfAPIKit.xcframework.zip`, anexa à Release, calcula o checksum e atualiza `Package.swift` na main. iOS pronto.

Você só precisa criar a tag. Nenhum passo manual.

---

## Estrutura

```
shared/
├── commonMain/         # Código compartilhado (Ktor, kotlinx.serialization)
│   ├── core/           # ApiClient, Endpoint, ApiError, TokenStore, LoadState
│   ├── config/         # ApiEnvironment (prod default)
│   ├── locations/      # LocationManager (expect) + DDDResolver
│   ├── platform/       # AppInfo (expect)
│   └── features/       # auth, cards, catalog, consult, customer, dreamshaper,
│                       # payments, portability, recharge, s3, schedule, viacep, config
├── androidMain/        # actuals: TokenStore (EncryptedSharedPreferences),
│                       # LocationManager (FusedLocationProvider), AppInfo
└── iosMain/            # actuals: TokenStore (Keychain), LocationManager (CLLocationManager), AppInfo
```

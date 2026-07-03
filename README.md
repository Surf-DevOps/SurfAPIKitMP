# SurfAPIKitMP

SDK Android da API Surf — em paridade com o `surfapikit-ios`.

Sem configurar nada no app: adicione a dependência e chame.

---

## Instalação (JitPack)

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
    implementation("com.github.Surf-DevOps.SurfAPIKitMP:shared:3.1.0")
}
```

Permissões já vêm declaradas no `AndroidManifest.xml` da lib (INTERNET, ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION). O `Context` é capturado automaticamente via `androidx.startup`.

---

## Uso

### Login + auth automática

```kotlin
import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.features.authentication.login.*

val res = SurfApiKit.login(LoginRequest(nuDocumento = "...", dsPassword = "...", coMvno = 1))
```

O `accessToken`/`refreshToken` são salvos automaticamente (EncryptedSharedPreferences) e injetados como `Authorization: Bearer ...` em todas as próximas chamadas. **Refresh em 401 é automático** via plugin Auth do Ktor — você não precisa orquestrar nada.

### Trocar ambiente (opcional)

Por padrão usa produção. Pra staging:

```kotlin
SurfApiKit.configure(environment = ApiEnvironment.STAGING)
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

Você só precisa criar a tag. Nenhum passo manual.

---

## Estrutura

```
shared/
└── src/main/          # Biblioteca Android (Ktor, kotlinx.serialization)
    ├── kotlin/io/github/surfdevops/surfapikit/
    │   ├── core/      # ApiClient, Endpoint, ApiError, TokenStore (EncryptedSharedPreferences), LoadState
    │   ├── config/    # ApiEnvironment (prod default)
    │   ├── locations/ # LocationManager (FusedLocationProvider) + DDDResolver
    │   ├── platform/  # AppInfo, AppContextHolder (androidx.startup)
    │   └── features/  # auth, cards, catalog, compreganhe, config, consult, customer,
    │                  # dreamshaper, mandouganhou, payments, portability, recharge,
    │                  # s3, schedule, viacep
    └── AndroidManifest.xml
```

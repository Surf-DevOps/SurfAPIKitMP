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
    implementation("com.github.Surf-DevOps.SurfAPIKitMP:shared:v4.6.0")
}
```

> **Atenção ao coordinate.** O `VERSION_NAME` atual é 4.0.3, mas o último
> artefato efetivamente publicado no JitPack é `v4.0.2` (mesmo conteúdo). Em
> 2026-08-12 o JitPack passou a responder `tagNotFound` para as tags `4.0.3` e
> `v4.0.3` mesmo existindo no GitHub — cache de refs do lado dele. Os aliases
> `4.0.1` e `4.0.2` (sem "v") também ficaram presos nesse estado e não se
> recuperam. Antes de trocar o pin, confirmar com
> `curl -sI https://jitpack.io/com/github/Surf-DevOps/SurfAPIKitMP/<versão>/SurfAPIKitMP-<versão>.pom`.

Permissões já vêm declaradas no `AndroidManifest.xml` da lib (INTERNET, ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION). O `Context` é capturado automaticamente via `androidx.startup`.

---

## Uso

### Login + auth automática

```kotlin
import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.features.authentication.login.*

val res = SurfApiKit.login(LoginRequest(nuDocumento = "...", dsPassword = "...", coMvno = 1))

// O endpoint responde HTTP 200 em dois formatos. Antes de validar a senha, o backend
// checa se o documento existe na base — se não existir, vem `sucesso = 1` com
// `resultado.documentoExiste = false` e SEM registros/selectionToken.
if (res.documentNotRegistered) {
    // documento não cadastrado: ofereça o cadastro
} else {
    val registros = res.resultado.registros.orEmpty()
    // segue para a seleção de linha
}
```

> **Breaking em 4.0.0** — `LoginSuccess.Resultado` teve os campos `nuDocumento`,
> `registros`, `selectionToken` e `tokenType` tornados **nullable** para acomodar a
> resposta "documento não cadastrado" (que antes estourava `ApiError.Decoding`).
> Use `documentNotRegistered` para distinguir os dois formatos.

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

### Cupons de Desconto

```kotlin
import io.github.surfdevops.surfapikit.features.coupons.validatecoupon.*
import io.github.surfdevops.surfapikit.features.payments.createpayment.*

// 1. Validar (preview - valores em REAIS)
val req = ValidateCouponRequest(
    idCupom = "PAGUEELEVE10",
    coMvno = 48,
    nuPlano = "100",
    vlRecarga = 50.0,
    nuDocumento = "12345678901"
)
val res = SurfApiKit.validateCoupon(req)
println("Desconto: R$ ${res.content.vlDescontoAplicado}")

// 2. Aplicar na Recarga (valor cheio em CENTAVOS)
val payReq = CreatePaymentRequest.cardPayment(
    coMsisdn = 5511999999999,
    cardId = "...",
    amount = 5000,
    nuPlano = "100",
    isRecurring = false,
    cupom = "PAGUEELEVE10"
)
val payRes = SurfApiKit.createPayment(payReq)
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

## Changelog

- **v4.6.0** — `ValidateSimPurchaseSuccess.resultado` ganhou `noPerfil: String?` (`"ESTUDANTE"`/`"PROFESSOR"`/`null`), espelhando a rota `GET /v2/customer/validate-purchase` do backend (commit `71f60e8`, 19/10/2026). `null` cobre CPF não beneficiário, perfil ambíguo ou falha na consulta (falha-soft).

## Estrutura

```
shared/
└── src/main/          # Biblioteca Android (Ktor, kotlinx.serialization)
    ├── kotlin/io/github/surfdevops/surfapikit/
    │   ├── core/      # ApiClient, Endpoint, ApiError, TokenStore (EncryptedSharedPreferences), LoadState
    │   ├── config/    # ApiEnvironment (prod default)
    │   ├── locations/ # LocationManager (FusedLocationProvider) + DDDResolver
    │   ├── platform/  # AppInfo, AppContextHolder (androidx.startup)
    │   └── features/  # auth, cards, catalog, compreganhe, config, consult, coupons, customer,
    │                  # dreamshaper, mandouganhou, payments, portability, recharge,
    │                  # s3, schedule, viacep
    └── AndroidManifest.xml
```

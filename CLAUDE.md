# Regras do projeto para o Claude

Ao concluir **qualquer alteração** solicitada neste repositório, o Claude deve, nesta ordem:

1. **Bump de versão** em `gradle.properties` (`VERSION_NAME`) seguindo SemVer:
   - `patch` (x.y.Z) → correções e ajustes sem mudança de API
   - `minor` (x.Y.0) → novas features compatíveis
   - `major` (X.0.0) → mudanças quebradoras de API
   Atualizar também a versão referenciada no `README.md` (snippet de dependência).
2. **Commit** na branch `main` com mensagem clara e objetiva (Conventional Commits; usar `feat!:`/`BREAKING CHANGE:` quando quebrar API).
3. **Push** da `main`.
4. **Criar a tag** `vX.Y.Z` e dar **push da tag** — é o que dispara o JitPack a compilar o AAR.

Se não houver alterações para commit, informar isso explicitamente. Se `commit`, `push` ou `tag` falhar, informar o erro e o motivo.

## Contexto

- Biblioteca **Android pura** (sem KMP/iOS). Mantida em paridade com o SDK iOS `surfapikit-ios` (`surftelecom/surfmobile/ios/surfapikit` no GitLab), que é a fonte da verdade para endpoints/models/campos.
- Publicada via **JitPack**: `com.github.Surf-DevOps.SurfAPIKitMP:shared:VERSION`.
- Código em `shared/src/main/kotlin/io/github/surfdevops/surfapikit/`.

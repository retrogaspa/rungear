# Como adicionar tênis no RunGear

Guia passo a passo para incluir novos modelos no catálogo do app.

---

## Visão geral

Existem **duas formas** de ter um tênis no app:

| Forma | Onde configura | Ícone | Aparece na lista do catálogo? |
|-------|----------------|-------|-------------------------------|
| **Catálogo** | Pasta `icons/` + arquivo Kotlin | PNG 100×100 que você coloca | ✅ Sim (Marca → Modelo → Cor) |
| **Personalizado** | Só dentro do app | Foto da galeria | ❌ Não (aba Personalizado) |

Este guia foca no **catálogo** — o fluxo que você usa para Fila, Olympikus, Asics etc.

---

## O que você precisa fazer (resumo)

1. Criar o **PNG** do tênis na pasta certa  
2. Registrar marca, modelo e cor em **`PredefinedSneakers.kt`**  
3. **Rebuild** no Android Studio  
4. Abrir o app → **Coleção** → escolher o tênis novo  

---

## Passo 1 — Preparar o ícone PNG

### Especificações

| Item | Valor |
|------|--------|
| Tamanho | **100 × 100 pixels** (quadrado) |
| Formato | **PNG** (preferido), WEBP ou JPG |
| Fundo | **Preto `#000000`** (combina com a badge na foto) |
| Conteúdo | Tênis **centralizado**, sem cortar |

### Onde salvar

Estrutura de pastas na **raiz do projeto**:

```
APP SHO/
└── icons/
    └── {marca}/
        └── {modelo}/
            └── {cor}.png
```

**Regras de nome:**

- Tudo **minúsculo**
- Sem espaços — use `_` (underline)
- Sem acentos — `azul_claro`, não `azul-claro`
- Marca/modelo/cor devem bater **exatamente** com as `key` do Kotlin

### Exemplo real (já no app)

```
icons/fila/racer_carbon_2/verde.png
icons/olympikus/corre_5/preto_amarelo.png
icons/asics/dynablast_5/azul_claro.png
icons/chunta/sn/beige.png
```

### Exemplo — adicionar Olympikus Corre 4 Preto/Branco

1. Crie a pasta (se não existir):

```
icons/olympikus/corre_4/
```

2. Salve o arquivo:

```
icons/olympikus/corre_4/preto_branco.png
```

> Dica: exporte do Photoshop, Figma ou recorte de foto do site da loja. O app encaixa o tênis automaticamente na badge.

---

## Passo 2 — Registrar no código

Abra:

```
app/src/main/java/com/appsho/sneakers/data/PredefinedSneakers.kt
```

### 2a) Nova cor em modelo que já existe

Se a **marca e o modelo** já estão no arquivo, só adicione uma entrada em `colors`:

```kotlin
PredefinedColorVariant(
    key = "preto_branco",           // = nome do arquivo sem .png
    name = "Preto e Branco",        // = texto que o usuário vê
    iconResId = R.drawable.sneaker_olympikus_corre_4_preto_branco  // fallback (opcional)
)
```

Se você **só** colocou o PNG em `icons/`, o app usa o PNG primeiro. O `iconResId` é fallback quando o PNG não existe — pode usar um drawable genérico ou criar um XML em `res/drawable/`.

### 2b) Modelo novo na marca existente

Dentro de `models` da marca:

```kotlin
PredefinedModel(
    key = "corre_4",
    name = "Corre 4",
    colors = listOf(
        PredefinedColorVariant(
            key = "preto_branco",
            name = "Preto e Branco",
            iconResId = R.drawable.sneaker_olympikus_corre_4_preto_branco
        )
    )
)
```

### 2c) Marca nova

Adicione um bloco `PredefinedBrand` inteiro:

```kotlin
PredefinedBrand(
    key = "nike",
    name = "Nike",
    models = listOf(
        PredefinedModel(
            key = "pegasus_41",
            name = "Pegasus 41",
            colors = listOf(
                PredefinedColorVariant(
                    key = "preto",
                    name = "Preto",
                    iconResId = R.drawable.sneaker_nike_pegasus_41_preto
                )
            )
        )
    )
)
```

Com PNG em:

```
icons/nike/pegasus_41/preto.png
```

### Tabela: o que cada `key` significa

| Campo Kotlin | Exemplo | Vira caminho do PNG |
|--------------|---------|---------------------|
| `brand.key` | `olympikus` | `icons/olympikus/...` |
| `model.key` | `corre_4` | `.../corre_4/...` |
| `color.key` | `preto_branco` | `.../preto_branco.png` |

Referência interna no banco:

```
predefined:olympikus:corre_4:preto_branco
```

---

## Passo 3 — Build / sincronizar

O Gradle **copia automaticamente** `icons/` → `app/src/main/assets/icons/` ao compilar.

1. Abra o projeto no **Android Studio**
2. **Build → Rebuild Project** (ou rode o app de novo no celular)
3. Não precisa copiar manualmente para `assets/`

Task responsável: `syncSneakerIcons` em `app/build.gradle.kts`.

---

## Passo 4 — Usar no app

1. Abra o RunGear  
2. Aba **Coleção**  
3. Toque em **Adicionar tênis** (ou equivalente na lista)  
4. Fluxo: **Marca → Modelo → Cor**  
5. Toque na variante — entra na sua coleção  
6. Aba **Criar** → escolha foto + tênis → badge na foto  

O texto na badge será **Marca + Modelo** (sem cor), ex.: `Olympikus Corre 4`.

---

## Checklist rápido

```
[ ] PNG 100×100, fundo preto
[ ] Arquivo em icons/{marca}/{modelo}/{cor}.png
[ ] keys iguais no Kotlin e no nome do arquivo
[ ] Entrada em PredefinedSneakers.kt
[ ] Rebuild no Android Studio
[ ] Testado em Coleção → Criar
```

---

## Personalizado (sem código)

Para um tênis que **não** está no catálogo:

1. Coleção → aba **Personalizado**  
2. Digite o nome  
3. Escolha foto do ícone na galeria  
4. Salvar  

O ícone fica **só no celular** — não usa a pasta `icons/`.

---

## Problemas comuns

### O tênis não aparece na lista do catálogo

- Falta entrada em `PredefinedSneakers.kt`, ou `key` errada.

### Ícone em branco / placeholder

- PNG no caminho errado — confira minúsculas e `_`  
- Esqueceu o Rebuild  
- Nome do arquivo diferente da `color.key` (ex.: `preto-amarelo` vs `preto_amarelo`)

### Badge sem ícone na foto final

- PNG corrompido ou muito pequeno  
- Teste abrir `app/src/main/assets/icons/...` após o build

### “Adicionado” desabilitado

- Essa variante já está na coleção — exclua antes ou use outra cor.

---

## Por onde começar?

Veja prioridades e pontuação de popularidade em:

**[CATALOGO_TENIS_BRASIL.md](./CATALOGO_TENIS_BRASIL.md)**

Sugestão imediata (alta pontuação, ainda não no app):

1. Olympikus Corre 4 — várias cores  
2. Nike Pegasus 41 — Preto, Branco  
3. ASICS Novablast 5 — Amarelo, Preto  
4. Mizuno Wave Rider 28 — Preto, Prata  

---

## Exemplo completo copy-paste

**Arquivo:** `icons/olympikus/corre_4/preto_branco.png`

**Kotlin** (dentro de `PredefinedBrand` olympikus → `models`):

```kotlin
PredefinedModel(
    key = "corre_4",
    name = "Corre 4",
    colors = listOf(
        PredefinedColorVariant(
            key = "preto_branco",
            name = "Preto e Branco",
            iconResId = R.drawable.sneaker_olympikus_corre_5_preto_amarelo  // temporário até ter drawable próprio
        )
    )
)
```

> Com o PNG no lugar certo, o app usa o PNG mesmo que o `iconResId` seja de outro modelo.

---

## Referência rápida de arquivos

| Arquivo | Função |
|---------|--------|
| `icons/` | Onde **você** coloca os PNGs |
| `app/src/main/assets/icons/` | Cópia automática (não editar) |
| `PredefinedSneakers.kt` | Catálogo Marca / Modelo / Cor |
| `icons/LEIA-ME.txt` | Lembrete curto na pasta de ícones |
| `SneakerIconLoader.kt` | Carrega PNG dos assets |
| `SneakerOverlayLabel.kt` | Texto da badge (marca + modelo) |

# Editor PES PSP (web) — lado Transfermarkt
1. Coloque os clubes em `data/teams.json` (`tm_id` = número no fim da URL `.../verein/614`).
2. Actions > "Atualizar elencos" > Run workflow (ou rode `python scripts/scrape.py` local).
3. O editor (GitHub Pages) lê `data/squads/<tm_id>.json` do próprio repositório.
Se o Transfermarkt bloquear o GitHub Actions, rode o script no seu PC e dê commit nos JSONs.

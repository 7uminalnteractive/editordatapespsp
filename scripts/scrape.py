"""Baixa elencos do Transfermarkt e grava em data/squads/<tm_id>.json.
Uso: python scripts/scrape.py            (todos os times de data/teams.json)
     python scripts/scrape.py 614 210    (só esses IDs)
"""
import json, re, sys, time, pathlib
import requests
from bs4 import BeautifulSoup

ROOT = pathlib.Path(__file__).resolve().parent.parent
BASE = "https://www.transfermarkt.com.br/x"
UA = ("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36")
S = requests.Session()
S.headers.update({"User-Agent": UA, "Accept-Language": "pt-BR,pt;q=0.9"})

def get(url):
    r = S.get(url, timeout=30)
    r.raise_for_status()
    return BeautifulSoup(r.text, "lxml")

def parse_squad(soup):
    """Lê a tabela do elenco guiando-se pelos títulos das colunas."""
    table = soup.select_one("table.items")
    if not table:
        return []
    heads = [th.get_text(" ", strip=True).lower() for th in table.select("thead th")]
    def idx(*names):
        for i, h in enumerate(heads):
            if any(n in h for n in names):
                return i
        return None
    i_age = idx("nasc", "idade", "born", "age")
    i_nat = idx("nac")
    i_h = idx("altura", "height")
    players = []
    for tr in table.select("tbody > tr"):
        tds = tr.find_all("td", recursive=False)
        if len(tds) < 4:
            continue
        a = tr.select_one("td.hauptlink a")
        if not a:
            continue
        num = tr.select_one("div.rn_nummer")
        pos_td = tr.select_one("table.inline-table tr:nth-of-type(2) td")
        age = born = None
        if i_age is not None and i_age < len(tds):
            t = tds[i_age].get_text(" ", strip=True)
            m = re.search(r"(\d{1,2}/\d{1,2}/\d{4})", t)
            born = m.group(1) if m else None
            m = re.search(r"\((\d+)\)", t)
            age = int(m.group(1)) if m else None
        nat = []
        if i_nat is not None and i_nat < len(tds):
            nat = [im.get("title") for im in tds[i_nat].select("img.flaggenrahmen") if im.get("title")]
        height = None
        if i_h is not None and i_h < len(tds):
            m = re.search(r"(\d)[,.](\d{2})", tds[i_h].get_text(strip=True))
            height = int(m.group(1)) * 100 + int(m.group(2)) if m else None
        players.append({
            "name": a.get_text(strip=True),
            "number": (num.get_text(strip=True) if num else None),
            "position": (pos_td.get_text(strip=True) if pos_td else None),
            "born": born, "age": age, "nationality": nat, "height_cm": height,
        })
    return players

def scrape(tm_id):
    # a aba "plus/1" traz altura; se falhar, cai para a página inicial
    try:
        players = parse_squad(get(f"{BASE}/kader/verein/{tm_id}/plus/1"))
    except requests.RequestException:
        players = []
    if not players:
        players = parse_squad(get(f"{BASE}/startseite/verein/{tm_id}"))
    return players

def main():
    teams = json.loads((ROOT / "data/teams.json").read_text(encoding="utf-8"))
    wanted = {int(x) for x in sys.argv[1:]}
    for t in teams:
        if wanted and t["tm_id"] not in wanted:
            continue
        try:
            players = scrape(t["tm_id"])
        except Exception as e:
            print("ERRO", t["tm_id"], t["name"], e); continue
        out = {"tm_id": t["tm_id"], "name": t["name"], "players": players}
        (ROOT / f"data/squads/{t['tm_id']}.json").write_text(
            json.dumps(out, ensure_ascii=False, indent=1), encoding="utf-8")
        print("ok", t["tm_id"], t["name"], len(players), "jogadores")
        time.sleep(4)  # educado com o site

if __name__ == "__main__":
    main()

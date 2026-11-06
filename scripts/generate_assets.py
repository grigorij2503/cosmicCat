"""Generate the original Cosmic Cat sprites. Requires Pillow (pip install Pillow)."""

from pathlib import Path
from functools import lru_cache
import random

from PIL import Image, ImageDraw, ImageOps


SIZE = 320
SCALE = 3
OUT = Path(__file__).resolve().parents[1] / "app/src/main/res/drawable-nodpi"
ICON = OUT.parent / "mipmap-xxxhdpi" / "ic_launcher.png"
DARK = "#302f50"
CREAM = "#fff1d8"
AQUA = "#91e8f6"
WHITE = "#ffffff"


def scaled(values):
    return tuple(round(value * SCALE) for value in values)


def curve(start, c1, c2, end):
    points = []
    for step in range(19):
        t = step / 18
        u = 1 - t
        points.append(tuple(u**3 * a + 3 * u**2 * t * b + 3 * u * t**2 * c + t**3 * d
                            for a, b, c, d in zip(start, c1, c2, end)))
    return points


@lru_cache(maxsize=32)
def gradient(color, width, height, highlight, shadow):
    glow = Image.radial_gradient("L").resize((width * 2, height * 2), Image.Resampling.BICUBIC)
    left, top = int(width * .68), int(height * .73)
    glow = glow.crop((left, top, left + width, top + height))
    return ImageOps.colorize(glow, highlight, shadow, color, blackpoint=0, whitepoint=235, midpoint=136)


class Art:
    def __init__(self):
        self.image = Image.new("RGBA", (SIZE * SCALE, SIZE * SCALE))
        self.draw = ImageDraw.Draw(self.image)

    def _fill(self, mask, color, lit, highlight="#fff4e7", shadow="#5e5576"):
        box = mask.getbbox()
        if lit and box:
            l, t, r, b = box
            self.image.paste(gradient(color, r - l, b - t, highlight, shadow), (l, t), mask.crop(box))
        else:
            self.image.paste(color, (0, 0, *self.image.size), mask)

    def ellipse(self, box, color, edge=0, lit=True):
        box = scaled(box)
        mask = Image.new("L", self.image.size)
        ImageDraw.Draw(mask).ellipse(box, fill=255)
        self._fill(mask, color, lit)
        if edge:
            self.draw.ellipse(box, outline=DARK, width=edge * SCALE)

    def round(self, box, radius, color, edge=0, lit=True, *, highlight="#fff4e7", shadow="#5e5576"):
        box = scaled(box)
        mask = Image.new("L", self.image.size)
        ImageDraw.Draw(mask).rounded_rectangle(box, radius * SCALE, fill=255)
        self._fill(mask, color, lit, highlight, shadow)
        if edge:
            self.draw.rounded_rectangle(box, radius * SCALE, outline=DARK, width=edge * SCALE)

    def poly(self, points, color, edge=0, lit=True):
        pts = [scaled(point) for point in points]
        mask = Image.new("L", self.image.size)
        ImageDraw.Draw(mask).polygon(pts, fill=255)
        self._fill(mask, color, lit)
        if edge:
            self.line(points + [points[0]], DARK, edge)

    def line(self, points, color, width):
        pts = [scaled(point) for point in points]
        radius = width * SCALE / 2
        self.draw.line(pts, fill=color, width=width * SCALE, joint="curve")
        for x, y in (pts[0], pts[-1]):
            self.draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=color)

    def write(self, name):
        self.image.resize((SIZE, SIZE), Image.Resampling.LANCZOS).save(OUT / f"{name}.png", optimize=True)


def cat_base():
    art = Art()
    art.ellipse((59, 293, 263, 311), "#22253d42", lit=False)
    tail = curve((207, 257), (291, 266), (294, 211), (275, 191))
    art.line(tail, DARK, 29)
    art.line(tail, "#e88857", 19)
    art.line(tail[-7:], "#b75d4c", 19)
    art.ellipse((67, 34, 253, 214), "#b7efff", 5)
    outline = (
        [(93, 101)] + curve((93, 101), (80, 60), (74, 31), (80, 14))
        + curve((80, 14), (83, 6), (89, 8), (97, 14))
        + [(136, 51)] + curve((136, 51), (149, 44), (171, 44), (184, 51))
        + [(223, 14)] + curve((223, 14), (231, 8), (237, 6), (240, 14))
        + curve((240, 14), (246, 31), (240, 60), (227, 101))
        + curve((227, 101), (246, 150), (211, 185), (160, 184))
        + curve((160, 184), (109, 185), (74, 150), (93, 101))
    )
    art.poly(outline, "#f69d5e", 5)
    art.poly([(89, 31), (111, 58), (96, 77)], "#f689a3", lit=False)
    art.poly([(231, 31), (209, 58), (224, 77)], "#f689a3", lit=False)
    art.ellipse((117, 119, 202, 171), "#ffdfac")
    for x in (124, 196):
        art.ellipse((x - 12, 95, x + 12, 124), DARK, lit=False)
        art.ellipse((x - 6, 97, x + 1, 105), WHITE, lit=False)
        art.ellipse((x + 2, 112, x + 6, 116), "#96ddd5", lit=False)
    for x, side in ((108, -1), (212, 1)):
        art.ellipse((x - 11, 127, x + 11, 136), "#f593a1", lit=False)
        art.line([(x + side * 1, 141), (x + side * 24, 143)], "#955660", 2)
        art.line([(x - side * 2, 148), (x + side * 19, 154)], "#955660", 2)
    art.poly([(150, 131), (157, 127), (163, 127), (170, 131), (160, 140)], "#c46983", 2, False)
    art.line(curve((142, 145), (150, 155), (156, 149), (160, 140))
             + curve((160, 140), (164, 149), (170, 155), (178, 145)), DARK, 3)
    return art


def suit(color):
    art = Art()
    for x in (103, 167):
        art.round((x, 249, x + 50, 299), 17, color, 5)
    for x in (76, 192):
        art.ellipse((x, 205, x + 52, 259), color, 5)
    art.round((106, 184, 214, 277), 29, CREAM, 5)
    art.round((123, 212, 197, 253), 12, DARK, lit=False)
    art.round((129, 218, 169, 246), 8, AQUA)
    art.ellipse((175, 218, 189, 232), "#fa6f7c", lit=False)
    art.ellipse((176, 236, 188, 246), "#ffd574", lit=False)
    art.poly(curve((77, 164), (127, 195), (193, 195), (243, 164)) + [(232, 194)]
             + curve((232, 194), (191, 219), (130, 219), (88, 194)), color, 5)
    art.line(curve((78, 116), (77, 133), (85, 148), (94, 154)), "#e6fcff", 5)
    art.line(curve((233, 79), (239, 89), (243, 101), (241, 114)), "#e6fcff", 4)
    for x in (112, 176):
        art.round((x, 279, x + 30, 285), 3, "#efe1ff", lit=False)
    return art


def rocket():
    art = Art()
    art.poly(curve((132, 237), (124, 268), (140, 296), (160, 315))
             + curve((160, 315), (180, 296), (196, 268), (188, 237)), "#fc8753", 4)
    art.poly(curve((145, 247), (140, 264), (149, 287), (160, 295))
             + curve((160, 295), (171, 287), (180, 264), (175, 247)), "#ffdc6e")
    for side in (-1, 1):
        wing = curve((195, 152), (243, 170), (256, 204), (258, 244)) + [(192, 216)]
        art.poly([(160 + side * (x - 160), y) for x, y in wing], "#ec6880", 5)
    art.round((123, 211, 197, 249), 12, DARK)
    art.poly(curve((105, 216), (90, 143), (105, 83), (126, 64)) + [(194, 64)]
             + curve((194, 64), (215, 83), (230, 143), (215, 216))
             + curve((215, 216), (185, 232), (134, 232), (105, 216)), "#68c8f4", 6)
    art.poly(curve((109, 86), (130, 23), (150, 21), (160, 22))
             + curve((160, 22), (170, 21), (190, 23), (211, 86))
             + curve((211, 86), (180, 96), (140, 96), (109, 86)), "#f37986", 5)
    art.round((134, 167, 186, 213), 12, "#87e99e", 4)
    art.round((161, 188, 178, 194), 3, DARK, lit=False)
    art.ellipse((126, 92, 194, 160), CREAM, 4)
    art.ellipse((132, 98, 188, 154), AQUA, 2)
    return art


def planet(name, color, seed):
    art = Art()
    if name == "saturn":
        art.line(curve((20, 200), (11, 131), (287, 87), (299, 117)), "#d6a177", 18)
    art.ellipse((52, 46, 268, 262), color, 5)
    accents = Art()
    if name in {"venus", "jupiter", "saturn", "neptune"}:
        for index in range(5):
            y = 84 + index * 31
            accents.line(curve((44, y - 13), (120, y + 22), (226, y + 26), (282, y - 9)),
                         "#fff6d253" if index % 2 == 0 else "#59406948", 17 + index % 2 * 8)
        if name == "jupiter":
            accents.ellipse((171, 168, 236, 195), "#cb6a58", 2)
    else:
        rng = random.Random(seed)
        for _ in range(15):
            x, y, r = rng.randint(72, 244), rng.randint(57, 250), rng.randint(7, 35)
            accents.ellipse((x - r, y - r * .65, x + r, y + r * .65), "#4c3d7446", lit=False)
            accents.line(curve((x - .65 * r, y + .2 * r), (x - .2 * r, y + .65 * r),
                               (x + .2 * r, y + .65 * r), (x + .65 * r, y + .2 * r)), "#fff4cc56", 2)
    accents.line(curve((78, 105), (91, 79), (118, 67), (142, 66)), "#fff7e599", 5)
    disk = Image.new("L", art.image.size)
    ImageDraw.Draw(disk).ellipse(scaled((56, 50, 264, 258)), fill=255)
    art.image.paste(Image.alpha_composite(art.image, accents.image), (0, 0), disk)
    if name == "saturn":
        ring = curve((20, 200), (37, 243), (311, 159), (299, 117))
        art.line(ring, "#aa7967", 23)
        art.line(ring, "#ffe1aa", 12)
    return art


def alien(index, color):
    art = Art()
    art.ellipse((60, 268, 264, 293), "#22253d42", lit=False)
    for side in (-1, 1):
        x = 160 + side * 54
        if index % 2 == 0:
            tip = x + side * 29
            art.line([(x, 109), (tip, 63)], DARK, 9)
            art.ellipse((tip - 14, 46, tip + 14, 75), "#ffd273", 4)
        else:
            art.poly([(160 + side * 89, 156), (160 + side * 109, 68), (x, 86), (x - side * 10, 122)], color, 5)
    art.ellipse((55, 188, 111, 245), color, 4)
    art.ellipse((209, 174, 266, 231), color, 4)
    for x in (107, 213):
        art.ellipse((x - 24, 244, x + 24, 281), color, 4)
    art.ellipse((76, 88, 244, 267), color, 6)
    for x in ((113, 160, 207) if index == 3 else (125, 195)):
        art.ellipse((x - 20, 132, x + 20, 184), CREAM, 3)
        art.ellipse((x - 10, 144, x + 12, 176), DARK, lit=False)
        art.ellipse((x - 6, 147, x + 2, 156), WHITE, lit=False)
    art.ellipse((132, 192, 188, 227), DARK, lit=False)
    art.ellipse((148, 213, 172, 226), "#f788a1", lit=False)
    for x in (103, 217):
        art.ellipse((x - 12, 191, x + 12, 203), "#f69ba7", lit=False)
    return art


def meteorite():
    art = Art()
    art.ellipse((41, 253, 280, 284), "#22253d42", lit=False)
    art.poly([(54, 210), (76, 135), (127, 79), (202, 65), (253, 117), (268, 198), (234, 248), (111, 266)], "#ffb840", 7)
    art.poly([(77, 135), (127, 80), (201, 66), (170, 169)], "#ffe27a")
    art.poly([(170, 169), (251, 118), (266, 197), (234, 245)], "#e69142")
    art.ellipse((106, 179, 143, 207), "#aa694d", 2)
    art.ellipse((201, 126, 229, 155), "#c98143", 2)
    art.ellipse((35, 244, 68, 268), "#ce8b66", 3)
    art.ellipse((251, 237, 280, 259), "#ce8b66", 3)
    for x, y in ((86, 83), (228, 67), (268, 106)):
        art.poly([(x, y - 9), (x + 3, y - 3), (x + 9, y), (x + 3, y + 3),
                  (x, y + 9), (x - 3, y + 3), (x - 9, y), (x - 3, y - 3)], "#fff3ab", lit=False)
    return art


def launcher():
    art = Art()
    art.round((8, 8, 312, 312), 62, "#6040a3", 4, highlight="#a06dd4", shadow="#332457")
    for x, y, radius in ((60, 58, 5), (275, 72, 5), (45, 244, 7), (269, 253, 6), (102, 40, 3)):
        art.ellipse((x - radius, y - radius, x + radius, y + radius), "#e0c8ff", lit=False)
    for x, y in ((48, 83), (272, 110), (62, 266), (246, 48)):
        art.line([(x - 7, y), (x + 7, y)], "#f6dfff", 2)
        art.line([(x, y - 7), (x, y + 7)], "#f6dfff", 2)

    art.line(curve((20, 176), (24, 147), (104, 130), (120, 150)), "#f8ba9e", 8)
    art.ellipse((36, 129, 107, 200), "#f68a8c", 3)
    art.line(curve((20, 176), (24, 193), (118, 179), (120, 150)), "#f8ba9e", 8)
    art.line(curve((23, 178), (39, 186), (110, 171), (118, 154)), "#fff0cf", 2)

    tail = curve((230, 270), (283, 274), (282, 236), (267, 213))
    art.line(tail, DARK, 21)
    art.line(tail, "#d6c8ec", 14)
    art.ellipse((99, 275, 268, 293), "#29214760", lit=False)
    art.round((126, 240, 180, 283), 15, "#f3e9ff", 5)
    art.round((191, 240, 243, 283), 15, "#f3e9ff", 5)
    art.ellipse((102, 203, 153, 256), "#f3e9ff", 5)
    art.ellipse((220, 203, 265, 256), "#f3e9ff", 5)
    art.round((131, 176, 240, 263), 31, "#f8efe4", 6)
    art.round((158, 218, 212, 248), 9, "#645b8f", 3)
    art.round((164, 225, 188, 241), 4, "#a4e8f3", 0)
    art.ellipse((194, 226, 205, 237), "#f79b9c", lit=False)

    art.poly([(123, 105), (109, 52), (121, 43), (160, 72)], "#f9f1e8", 5)
    art.poly([(211, 72), (245, 45), (257, 53), (247, 108)], "#f9f1e8", 5)
    art.poly([(122, 58), (146, 76), (127, 93)], "#d7a6d5", lit=False)
    art.poly([(243, 58), (227, 77), (242, 92)], "#d7a6d5", lit=False)
    art.ellipse((114, 65, 255, 208), "#f8efe6", 6)
    art.ellipse((133, 84, 236, 186), "#b5add7", 4)
    art.ellipse((142, 93, 226, 178), "#36354f", 3)
    art.line(curve((151, 128), (153, 116), (161, 108), (177, 104)), "#dbd6f0", 5)
    art.ellipse((194, 151, 212, 160), "#646382", lit=False)
    return art


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    cat = cat_base()
    cat.write("cat_base")
    suit("#a994fa").write("cat_suit_overlay")
    cat.image = Image.alpha_composite(cat.image, suit("#ef6373").image)
    cat.write("catx_npc")
    icon = launcher()
    ICON.parent.mkdir(parents=True, exist_ok=True)
    icon.image.resize((192, 192), Image.Resampling.LANCZOS).save(ICON, optimize=True)
    rocket().write("rocket_base")
    meteorite().write("golden_meteorite")
    palette = dict(moon="#cbd7ee", mars="#f78360", pluto="#f8d789", venus="#ffb36e",
                   jupiter="#ecaf8e", saturn="#eac58d", neptune="#7c9df7")
    for index, (name, color) in enumerate(palette.items()):
        planet(name, color, 63 + index).write(f"planet_{name}")
    for index, color in enumerate(("#67dfa8", "#f494bb", "#ffca72", "#b599fb", "#68cced")):
        alien(index, color).write(f"alien_friend_{index + 1}")
    print("Zapisano 17 bitmap PNG oraz 1 plik ikony.")


if __name__ == "__main__":
    main()

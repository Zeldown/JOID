import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';

const DOCUMENTATION = fileURLToPath(new URL('..', import.meta.url));
const LOGO = join(DOCUMENTATION, 'brand', 'logo.png');
const PUBLIC = join(DOCUMENTATION, 'public');

for (const [name, size] of [['favicon-16.png', 16], ['favicon-32.png', 32], ['apple-touch-icon.png', 180]]) {
	await sharp(LOGO).resize(size, size).png().toFile(join(PUBLIC, name));
}
await sharp(LOGO).resize(256, 256).png().toFile(join(DOCUMENTATION, 'src', 'assets', 'logo.png'));

const width = 1200;
const height = 630;
const mark = await sharp(LOGO).resize(280, 280).png().toBuffer();
const text = Buffer.from(`<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}"><rect width="100%" height="100%" fill="#17171c"/><text x="480" y="300" font-family="Montserrat, Arial, sans-serif" font-size="120" font-weight="700" fill="#ffffff">JOID</text><text x="480" y="370" font-family="Montserrat, Arial, sans-serif" font-size="34" fill="#a1a1aa">Interfaces in Java, sharp on every screen</text></svg>`);
await sharp(text).composite([{ input: mark, left: 140, top: 175 }]).png().toFile(join(PUBLIC, 'og.png'));
console.log('brand assets written');

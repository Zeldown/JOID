DEL /S /Q output
mkdir output
"msdf-atlas-gen.exe" -font font.ttf -charset "charset.txt" -dimensions 2048 2048 -imageout output/font.png -json output/font.json -type msdf -pxrange 24 -coloringstrategy distance
DEL font.ttf
import json
import os
from datetime import datetime, timezone
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak

with open("resultados/resumen.json", encoding="utf-8") as archivo:
    datos = json.load(archivo)

archivo_pdf = "resultados/reporte_pruebas_consolidado.pdf"
documento = SimpleDocTemplate(archivo_pdf, pagesize=A4, rightMargin=18*mm, leftMargin=18*mm, topMargin=18*mm, bottomMargin=18*mm)
estilos = getSampleStyleSheet()
estilos.add(ParagraphStyle(name="TituloCentro", parent=estilos["Title"], alignment=TA_CENTER, textColor=colors.HexColor("#17365D")))
elementos = [
    Paragraph("FERRETERÍA CHARITO", estilos["Heading2"]),
    Paragraph("Reporte consolidado de pruebas de software", estilos["TituloCentro"]),
    Spacer(1, 8*mm),
    Paragraph("Ejecución: " + str(datos.get("ejecucion", "N/D")), estilos["Normal"]),
    Paragraph("Fecha UTC: " + (datos.get("fecha_utc") or datetime.now(timezone.utc).isoformat()), estilos["Normal"]),
    Paragraph("Backend: " + datos["backend"], estilos["Normal"]),
    Paragraph("Frontend: " + datos["frontend"], estilos["Normal"]),
    Paragraph("Ejecución CI: " + datos.get("url", ""), estilos["Normal"]),
    Spacer(1, 8*mm),
    Paragraph("Resultados por componente", estilos["Heading2"])
]
filas = [["Componente", "Prueba", "Resultado"]]
for item in datos["resultados"]:
    filas.append([item["area"], item["prueba"], str(item["estado"]).upper()])
tabla = Table(filas, colWidths=[32*mm, 93*mm, 45*mm], repeatRows=1)
tabla.setStyle(TableStyle([
    ("BACKGROUND",(0,0),(-1,0),colors.HexColor("#17365D")),
    ("TEXTCOLOR",(0,0),(-1,0),colors.white),
    ("GRID",(0,0),(-1,-1),0.4,colors.HexColor("#B7C9D6")),
    ("VALIGN",(0,0),(-1,-1),"MIDDLE"),
    ("PADDING",(0,0),(-1,-1),7),
    ("ROWBACKGROUNDS",(0,1),(-1,-1),[colors.white,colors.HexColor("#EEF3F8")])
]))
elementos.extend([tabla, Spacer(1, 8*mm)])
total = len(datos["resultados"])
correctas = sum(1 for x in datos["resultados"] if x["estado"] == "success")
fallidas = sum(1 for x in datos["resultados"] if x["estado"] == "failure")
omitidas = total - correctas - fallidas
porcentaje = round(correctas * 100 / total, 2) if total else 0
elementos.append(Paragraph("Resumen", estilos["Heading2"]))
elementos.append(Paragraph(f"Comprobaciones: {total} | Exitosas: {correctas} | Fallidas: {fallidas} | Omitidas: {omitidas} | Éxito: {porcentaje}%", estilos["Normal"]))
if fallidas or omitidas:
    elementos.append(Spacer(1, 4*mm))
    elementos.append(Paragraph("La ejecución contiene fallos u omisiones. Consulte los registros de GitHub Actions para revisar el detalle técnico de cada paso.", estilos["Normal"]))
else:
    elementos.append(Paragraph("Todas las comprobaciones registradas finalizaron correctamente.", estilos["Normal"]))
documento.build(elementos)

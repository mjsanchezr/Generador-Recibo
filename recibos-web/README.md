# CARDAMOMO Y CELERY — Generador de Recibos de Pago

🌐 **[Abrir aplicación en GitHub Pages](https://mjsanchezr.github.io/CARDAMOMO-Y-CELERY-GENERADOR/)**

Este proyecto es un generador de recibos de pago oficial para la empresa **CARDAMOMO Y CELERY, C.A.** Permite crear documentos `.docx` descargables directamente desde el navegador, sin necesidad de instalar ningún software. Soporta tres modelos de recibo: **Ordinario** (Salario y Cestaticket), **Especial** (Día Feriado), y **Extra** (Hora Extraordinaria); todos con cálculos automáticos aplicados a la tasa BCV oficial del día.

El proyecto cuenta con dos componentes: un **backend en Java 22** (Apache POI + Maven) que sirve como lógica de referencia y genera documentos desde la terminal, y un **frontend web moderno** construido con React, Vite, TypeScript, Tailwind CSS, Framer Motion y la librería `docx.js`, capaz de generar y descargar los recibos directamente en el navegador sin ningún servidor.

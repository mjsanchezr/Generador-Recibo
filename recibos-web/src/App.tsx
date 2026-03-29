import { useState, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { motion, AnimatePresence } from 'framer-motion';
import { FileDown, Calendar, User, AlignLeft, ReceiptText, Banknote, Landmark } from 'lucide-react';
import { generarDocumento } from './lib/generadorDocumento';
import { calcularRecibo, type TipoRecibo } from './lib/calculadora';

const schema = z.object({
  tipo: z.enum(["ORDINARIO", "FERIADO", "HORA_EXTRA"]),
  nombre: z.string().min(3, "El nombre debe tener al menos 3 caracteres"),
  cedula: z.string().min(5, "Cédula inválida (ej. V-12.345.678)"),
  fechaSTR: z.string().refine((val) => !isNaN(Date.parse(val)), "Fecha inválida"),
  tasaBCV: z.number({ message: "Debe ser un número válido" }).min(0.01, "La tasa debe ser mayor a 0"),
});

type FormValues = z.infer<typeof schema>;

function App() {
  const [isGenerating, setIsGenerating] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  const { register, handleSubmit, watch, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      tipo: "ORDINARIO",
      nombre: "",
      cedula: "V-",
      fechaSTR: new Date().toISOString().substring(0, 10),
      tasaBCV: 36.50
    }
  });

  const tipo = watch("tipo") as TipoRecibo;
  const nombre = watch("nombre");
  const cedula = watch("cedula");
  const tasaBCV = watch("tasaBCV");

  const calcs = useMemo(() => {
    return calcularRecibo(tipo, tasaBCV || 0);
  }, [tipo, tasaBCV]);

  // Labels dynamically based on type
  let lblBase = "", lblCesta = "", usdBase = 0, usdCesta = 0;
  if (tipo === "ORDINARIO") { lblBase = "Salario Básico"; lblCesta = "Cestaticket"; usdBase = 30; usdCesta = 40; }
  else if (tipo === "FERIADO") { lblBase = "Base Feriado"; lblCesta = "Recargo (50%)"; usdBase = 1.0; usdCesta = 0.5; }
  else { lblBase = "Base Hora"; lblCesta = "Recargo (50%)"; usdBase = 0.125; usdCesta = 0.0625; }

  const onSubmit = async (data: FormValues) => {
    setIsGenerating(true);
    setSuccessMsg('');
    try {
      const fechaObj = new Date(data.fechaSTR);
      fechaObj.setMinutes(fechaObj.getMinutes() + fechaObj.getTimezoneOffset());
      await generarDocumento({
        tipo: data.tipo,
        nombre: data.nombre.toUpperCase(),
        cedula: data.cedula.toUpperCase(),
        fecha: fechaObj,
        tasaBCV: data.tasaBCV
      });
      setSuccessMsg(`Documento del modelo ${data.tipo} generado exitosamente.`);
      setTimeout(() => setSuccessMsg(''), 5000);
    } catch (err) {
      console.error(err);
      alert("Se produjo un error crítico generando el recibo. Verifica la consola.");
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className="min-h-screen py-10 px-4 flex flex-col items-center justify-center font-sans">

      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="text-center mb-10 w-full max-w-5xl"
      >
        <div className="inline-flex items-center justify-center p-3 bg-cyan-500/10 rounded-2xl mb-4 border border-cyan-500/20 shadow-[0_0_30px_rgba(6,182,212,0.3)]">
          <ReceiptText className="w-10 h-10 text-cyan-400" />
        </div>
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight text-transparent bg-clip-text bg-gradient-to-br from-white to-slate-400">
          CARDAMOMO Y CELERY
        </h1>
        <p className="mt-2 text-cyan-400 font-medium tracking-wide">GENERADOR DE RECIBOS DE PAGO</p>
      </motion.div>

      <div className="w-full max-w-5xl grid grid-cols-1 lg:grid-cols-12 gap-8">

        {/* FORMULARIO */}
        <motion.div
          initial={{ opacity: 0, x: -30 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.1 }}
          className="lg:col-span-7 glass-card p-6 md:p-8"
        >
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">

            {/* Tipo de Recibo */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-300 flex items-center gap-2"><AlignLeft className="w-4 h-4" /> Seleccione el Modelo</label>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 relative">
                {[
                  { id: "ORDINARIO", label: "Ordinario", desc: "Salario y Cestaticket" },
                  { id: "FERIADO", label: "Especial", desc: "Día Feriado" },
                  { id: "HORA_EXTRA", label: "Extra", desc: "Hora Extraordinaria" }
                ].map((opt) => (
                  <label key={opt.id} className={`cursor-pointer border rounded-xl p-4 flex flex-col items-start transition-all relative overflow-hidden ${tipo === opt.id ? 'border-cyan-500 bg-cyan-500/10 shadow-[0_0_15px_rgba(6,182,212,0.2)]' : 'border-slate-700/50 bg-slate-900/30 hover:bg-slate-800/50'}`}>
                    <input type="radio" value={opt.id} {...register("tipo")} className="sr-only" />
                    <span className={`font-semibold ${tipo === opt.id ? 'text-cyan-400' : 'text-slate-200'}`}>{opt.label}</span>
                    <span className="text-xs text-slate-400 mt-1">{opt.desc}</span>
                    {tipo === opt.id && <motion.div layoutId="activeModel" className="absolute left-0 top-0 w-1 h-full bg-cyan-500" />}
                  </label>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Empleado */}
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-300 flex items-center gap-2"><User className="w-4 h-4" /> Nombre del Empleado</label>
                <input type="text" {...register("nombre")} className="glass-input w-full uppercase" placeholder="Ej. YSAURA FAGUNDEZ" />
                {errors.nombre && <p className="text-xs text-rose-400">{errors.nombre.message}</p>}
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-300 flex items-center gap-2"><Landmark className="w-4 h-4" /> Cédula de Identidad</label>
                <input type="text" {...register("cedula")} className="glass-input w-full uppercase" placeholder="Ej. V-12.345.678" />
                {errors.cedula && <p className="text-xs text-rose-400">{errors.cedula.message}</p>}
              </div>

              {/* Fecha y Tasa */}
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-300 flex items-center gap-2"><Calendar className="w-4 h-4" /> Fecha del Recibo</label>
                <input type="date" {...register("fechaSTR")} className="glass-input w-full [color-scheme:dark]" />
                {errors.fechaSTR && <p className="text-xs text-rose-400">{errors.fechaSTR.message}</p>}
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-300 flex items-center gap-2"><Banknote className="w-4 h-4" /> Tasa BCV Oficial</label>
                <input type="number" step="0.01" {...register("tasaBCV", { valueAsNumber: true })} className="glass-input w-full" placeholder="Ej. 36.50" />
                {errors.tasaBCV && <p className="text-xs text-rose-400">{errors.tasaBCV.message}</p>}
              </div>
            </div>

            <button
              type="submit"
              disabled={isGenerating}
              className="btn-primary w-full mt-6 flex items-center justify-center gap-2 text-lg h-14"
            >
              {isGenerating ? (
                <span className="w-6 h-6 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <><FileDown className="w-6 h-6" /> Descargar Documento .docx</>
              )}
            </button>

            <AnimatePresence>
              {successMsg && (
                <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="text-emerald-400 text-center text-sm font-medium pt-2 overflow-hidden">
                  ✅ {successMsg}
                </motion.div>
              )}
            </AnimatePresence>
          </form>
        </motion.div>

        {/* PREVIEW EN VIVO */}
        <motion.div
          initial={{ opacity: 0, x: 30 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.2 }}
          className="lg:col-span-5 relative"
        >
          <div className="sticky top-10 glass-card p-6 md:p-8 flex flex-col h-[calc(100%-2rem)] min-h-[400px] bg-gradient-to-b from-slate-900/80 to-slate-950/80">
            <h3 className="text-lg font-bold text-slate-200 mb-6 flex items-center gap-2 border-b border-slate-700/50 pb-4">
              <span className="relative flex h-3 w-3">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-cyan-500"></span>
              </span>
              Vista Previa Matemática
            </h3>

            <div className="space-y-4 mb-8 flex-1">
              <div className="flex justify-between items-center bg-slate-800/30 p-3 rounded-lg border border-slate-700/30">
                <span className="text-slate-400 text-sm">{lblBase} <span className="text-slate-500 text-xs">(${usdBase} × {tasaBCV || 0})</span></span>
                <span className="font-mono text-slate-200">Bs. {calcs.salarioBs.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center bg-slate-800/30 p-3 rounded-lg border border-slate-700/30">
                <span className="text-slate-400 text-sm">{lblCesta} <span className="text-slate-500 text-xs">(${usdCesta} × {tasaBCV || 0})</span></span>
                <span className="font-mono text-slate-200">Bs. {calcs.cestaticketBs.toFixed(2)}</span>
              </div>

              <motion.div
                key={calcs.totalBs}
                initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
                className="mt-6 flex justify-between items-center bg-gradient-to-r from-cyan-950/50 to-emerald-950/50 p-4 rounded-xl border border-cyan-500/30 shadow-[inset_0_0_20px_rgba(6,182,212,0.1)]"
              >
                <span className="text-cyan-200 font-semibold text-sm">TOTAL A PAGAR</span>
                <span className="font-mono text-xl md:text-2xl font-bold text-emerald-400">Bs. {calcs.totalBs.toFixed(2)}</span>
              </motion.div>
            </div>

            <div className="text-xs text-slate-500 text-center flex flex-col gap-1 border-t border-slate-800 pt-4 mt-auto">
              <span>Empleado: {nombre || '—'} ({cedula || '—'})</span>
              <span>Tasa Oficial: Bs. {tasaBCV || '0.00'} / USD</span>
            </div>
          </div>
        </motion.div>

      </div>
    </div>
  );
}

export default App;

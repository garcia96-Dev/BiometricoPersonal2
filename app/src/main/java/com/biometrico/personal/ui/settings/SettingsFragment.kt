package com.biometrico.personal.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.biometrico.personal.data.model.ConfiguracionHorario
import com.biometrico.personal.databinding.FragmentSettingsBinding
import java.time.LocalDate

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private var configActual: ConfiguracionHorario = ConfiguracionHorario()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarSpinnerJornada()
        observarConfiguracion()
        configurarListeners()
        mostrarJornadaVigente()
    }

    private fun mostrarJornadaVigente() {
        val hoy = LocalDate.now()
        val jornadaVigente = when {
            hoy >= LocalDate.of(2026, 7, 15) -> "42 horas/semana (desde Jul 2026)"
            hoy >= LocalDate.of(2025, 7, 15) -> "44 horas/semana (desde Jul 2025) ← VIGENTE"
            hoy >= LocalDate.of(2024, 7, 15) -> "46 horas/semana (desde Jul 2024)"
            hoy >= LocalDate.of(2023, 7, 15) -> "47 horas/semana (desde Jul 2023)"
            else -> "48 horas/semana (anterior)"
        }
        binding.tvJornadaVigente.text = "📋 Jornada legal vigente hoy: $jornadaVigente"
    }

    private fun configurarSpinnerJornada() {
        val opciones = viewModel.jornadasLegales.map { "${it.label} — ${it.descripcion}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJornada.adapter = adapter
    }

    private fun observarConfiguracion() {
        viewModel.configuracion.observe(viewLifecycleOwner) { config ->
            config?.let {
                configActual = it
                llenarFormulario(it)
            }
        }
    }

    private fun llenarFormulario(c: ConfiguracionHorario) {
        // Datos personales
        binding.etNombreTrabajador.setText(c.nombreTrabajador)
        binding.etEmpresa.setText(c.empresaNombre)
        binding.etCargo.setText(c.cargo)

        // Jornada legal
        val idx = viewModel.jornadasLegales.indexOfFirst { it.horas == c.jornadaLey }
        if (idx >= 0) binding.spinnerJornada.setSelection(idx)
        binding.switchJornadaPersonalizada.isChecked = c.usarJornadaPersonalizada
        binding.etHorasPersonalizadas.setText(c.horasSemanalesPersonalizadas.toInt().toString())
        binding.layoutHorasPersonalizadas.visibility =
            if (c.usarJornadaPersonalizada) View.VISIBLE else View.GONE

        // Almuerzo
        binding.sliderAlmuerzo.value = c.duracionAlmuerzo.toFloat()
        binding.tvAlmuerzoValor.text = "${c.duracionAlmuerzo} min"

        // Horario por día
        binding.switchLunes.isChecked = c.trabajaLunes
        binding.tvLunesEntrada.text = c.lunesEntrada
        binding.tvLunesSalida.text = c.lunesSalida
        binding.etLunesHoras.setText(c.lunesHoras.toString())
        binding.layoutLunesHorario.visibility = if (c.trabajaLunes) View.VISIBLE else View.GONE

        binding.switchMartes.isChecked = c.trabajaMartes
        binding.tvMartesEntrada.text = c.martesEntrada
        binding.tvMartesSalida.text = c.martesSalida
        binding.etMartesHoras.setText(c.martesHoras.toString())
        binding.layoutMartesHorario.visibility = if (c.trabajaMartes) View.VISIBLE else View.GONE

        binding.switchMiercoles.isChecked = c.trabajaMiercoles
        binding.tvMiercolesEntrada.text = c.miercolesEntrada
        binding.tvMiercolesSalida.text = c.miercolesSalida
        binding.etMiercolesHoras.setText(c.miercolesHoras.toString())
        binding.layoutMiercolesHorario.visibility = if (c.trabajaMiercoles) View.VISIBLE else View.GONE

        binding.switchJueves.isChecked = c.trabajaJueves
        binding.tvJuevesEntrada.text = c.juevesEntrada
        binding.tvJuevesSalida.text = c.juevesSalida
        binding.etJuevesHoras.setText(c.juevesHoras.toString())
        binding.layoutJuevesHorario.visibility = if (c.trabajaJueves) View.VISIBLE else View.GONE

        binding.switchViernes.isChecked = c.trabajaViernes
        binding.tvViernesEntrada.text = c.viernesEntrada
        binding.tvViernesSalida.text = c.viernesSalida
        binding.etViernesHoras.setText(c.viernesHoras.toString())
        binding.layoutViernesHorario.visibility = if (c.trabajaViernes) View.VISIBLE else View.GONE

        binding.switchSabado.isChecked = c.trabajaSabado
        binding.tvSabadoEntrada.text = c.sabadoEntrada
        binding.tvSabadoSalida.text = c.sabadoSalida
        binding.etSabadoHoras.setText(c.sabadoHoras.toString())
        binding.layoutSabadoHorario.visibility = if (c.trabajaSabado) View.VISIBLE else View.GONE

        binding.switchDomingo.isChecked = c.trabajaDomingo
        binding.tvDomingoEntrada.text = c.domingoEntrada
        binding.tvDomingoSalida.text = c.domingoSalida
        binding.etDomingoHoras.setText(c.domingoHoras.toString())
        binding.layoutDomingoHorario.visibility = if (c.trabajaDomingo) View.VISIBLE else View.GONE

        // Tolerancias
        binding.sliderToleranciaEntrada.value = c.toleranciaEntradaMin.toFloat()
        binding.tvToleranciaEntradaValor.text = "${c.toleranciaEntradaMin} min"
        binding.sliderToleranciaSalida.value = c.toleranciaSalidaMin.toFloat()
        binding.tvToleranciaSalidaValor.text = "${c.toleranciaSalidaMin} min"

        // Nocturna
        binding.tvInicioNocturna.text = c.inicioJornadaNocturna
        binding.tvFinNocturna.text = c.finJornadaNocturna

        // Biométrico
        binding.switchHuella.isChecked = c.usarHuella
        binding.switchPin.isChecked = c.usarPin

        // Notificaciones
        binding.switchNotifEntrada.isChecked = c.notificarEntrada
        binding.switchNotifSalida.isChecked = c.notificarSalida
        binding.sliderMinutosNotif.value = c.minutosAntesNotificacion.toFloat()
        binding.tvMinutosNotifValor.text = "${c.minutosAntesNotificacion} min antes"
    }

    private fun configurarListeners() {
        binding.switchJornadaPersonalizada.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutHorasPersonalizadas.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Toggles de días — mostrar/ocultar horario de ese día
        binding.switchLunes.setOnCheckedChangeListener { _, v -> binding.layoutLunesHorario.visibility = if (v) View.VISIBLE else View.GONE }
        binding.switchMartes.setOnCheckedChangeListener { _, v -> binding.layoutMartesHorario.visibility = if (v) View.VISIBLE else View.GONE }
        binding.switchMiercoles.setOnCheckedChangeListener { _, v -> binding.layoutMiercolesHorario.visibility = if (v) View.VISIBLE else View.GONE }
        binding.switchJueves.setOnCheckedChangeListener { _, v -> binding.layoutJuevesHorario.visibility = if (v) View.VISIBLE else View.GONE }
        binding.switchViernes.setOnCheckedChangeListener { _, v -> binding.layoutViernesHorario.visibility = if (v) View.VISIBLE else View.GONE }
        binding.switchSabado.setOnCheckedChangeListener { _, v -> binding.layoutSabadoHorario.visibility = if (v) View.VISIBLE else View.GONE }
        binding.switchDomingo.setOnCheckedChangeListener { _, v -> binding.layoutDomingoHorario.visibility = if (v) View.VISIBLE else View.GONE }

        // Time pickers por día
        binding.btnLunesEntrada.setOnClickListener { timePicker(configActual.lunesEntrada) { binding.tvLunesEntrada.text = it } }
        binding.btnLunesSalida.setOnClickListener { timePicker(configActual.lunesSalida) { binding.tvLunesSalida.text = it } }
        binding.btnMartesEntrada.setOnClickListener { timePicker(configActual.martesEntrada) { binding.tvMartesEntrada.text = it } }
        binding.btnMartesSalida.setOnClickListener { timePicker(configActual.martesSalida) { binding.tvMartesSalida.text = it } }
        binding.btnMiercolesEntrada.setOnClickListener { timePicker(configActual.miercolesEntrada) { binding.tvMiercolesEntrada.text = it } }
        binding.btnMiercolesSalida.setOnClickListener { timePicker(configActual.miercolesSalida) { binding.tvMiercolesSalida.text = it } }
        binding.btnJuevesEntrada.setOnClickListener { timePicker(configActual.juevesEntrada) { binding.tvJuevesEntrada.text = it } }
        binding.btnJuevesSalida.setOnClickListener { timePicker(configActual.juevesSalida) { binding.tvJuevesSalida.text = it } }
        binding.btnViernesEntrada.setOnClickListener { timePicker(configActual.viernesEntrada) { binding.tvViernesEntrada.text = it } }
        binding.btnViernesSalida.setOnClickListener { timePicker(configActual.viernesSalida) { binding.tvViernesSalida.text = it } }
        binding.btnSabadoEntrada.setOnClickListener { timePicker(configActual.sabadoEntrada) { binding.tvSabadoEntrada.text = it } }
        binding.btnSabadoSalida.setOnClickListener { timePicker(configActual.sabadoSalida) { binding.tvSabadoSalida.text = it } }
        binding.btnDomingoEntrada.setOnClickListener { timePicker(configActual.domingoEntrada) { binding.tvDomingoEntrada.text = it } }
        binding.btnDomingoSalida.setOnClickListener { timePicker(configActual.domingoSalida) { binding.tvDomingoSalida.text = it } }

        // Nocturna
        binding.btnPickerInicioNocturna.setOnClickListener { timePicker(configActual.inicioJornadaNocturna) { binding.tvInicioNocturna.text = it } }
        binding.btnPickerFinNocturna.setOnClickListener { timePicker(configActual.finJornadaNocturna) { binding.tvFinNocturna.text = it } }

        // Sliders
        binding.sliderAlmuerzo.addOnChangeListener { _, v, _ -> binding.tvAlmuerzoValor.text = "${v.toInt()} min" }
        binding.sliderToleranciaEntrada.addOnChangeListener { _, v, _ -> binding.tvToleranciaEntradaValor.text = "${v.toInt()} min" }
        binding.sliderToleranciaSalida.addOnChangeListener { _, v, _ -> binding.tvToleranciaSalidaValor.text = "${v.toInt()} min" }
        binding.sliderMinutosNotif.addOnChangeListener { _, v, _ -> binding.tvMinutosNotifValor.text = "${v.toInt()} min antes" }

        binding.btnResetRecargos.setOnClickListener {
            binding.tvInicioNocturna.text = "19:00"
            binding.tvFinNocturna.text = "06:00"
            Toast.makeText(context, "Recargos restablecidos según ley", Toast.LENGTH_SHORT).show()
        }

        binding.btnGuardar.setOnClickListener { guardarConfiguracion() }
    }

    private fun timePicker(horaActual: String, callback: (String) -> Unit) {
        val partes = horaActual.split(":")
        TimePickerDialog(requireContext(), { _, h, m ->
            callback("%02d:%02d".format(h, m))
        }, partes[0].toInt(), partes[1].toInt(), true).show()
    }

    private fun guardarConfiguracion() {
        val jornadaIdx = binding.spinnerJornada.selectedItemPosition
        val config = configActual.copy(
            nombreTrabajador = binding.etNombreTrabajador.text.toString(),
            empresaNombre = binding.etEmpresa.text.toString(),
            cargo = binding.etCargo.text.toString(),
            jornadaLey = viewModel.jornadasLegales[jornadaIdx].horas,
            usarJornadaPersonalizada = binding.switchJornadaPersonalizada.isChecked,
            horasSemanalesPersonalizadas = binding.etHorasPersonalizadas.text.toString().toFloatOrNull() ?: 44f,
            duracionAlmuerzo = binding.sliderAlmuerzo.value.toInt(),

            trabajaLunes = binding.switchLunes.isChecked,
            lunesEntrada = binding.tvLunesEntrada.text.toString(),
            lunesSalida = binding.tvLunesSalida.text.toString(),
            lunesHoras = binding.etLunesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaMartes = binding.switchMartes.isChecked,
            martesEntrada = binding.tvMartesEntrada.text.toString(),
            martesSalida = binding.tvMartesSalida.text.toString(),
            martesHoras = binding.etMartesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaMiercoles = binding.switchMiercoles.isChecked,
            miercolesEntrada = binding.tvMiercolesEntrada.text.toString(),
            miercolesSalida = binding.tvMiercolesSalida.text.toString(),
            miercolesHoras = binding.etMiercolesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaJueves = binding.switchJueves.isChecked,
            juevesEntrada = binding.tvJuevesEntrada.text.toString(),
            juevesSalida = binding.tvJuevesSalida.text.toString(),
            juevesHoras = binding.etJuevesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaViernes = binding.switchViernes.isChecked,
            viernesEntrada = binding.tvViernesEntrada.text.toString(),
            viernesSalida = binding.tvViernesSalida.text.toString(),
            viernesHoras = binding.etViernesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaSabado = binding.switchSabado.isChecked,
            sabadoEntrada = binding.tvSabadoEntrada.text.toString(),
            sabadoSalida = binding.tvSabadoSalida.text.toString(),
            sabadoHoras = binding.etSabadoHoras.text.toString().toFloatOrNull() ?: 4f,

            trabajaDomingo = binding.switchDomingo.isChecked,
            domingoEntrada = binding.tvDomingoEntrada.text.toString(),
            domingoSalida = binding.tvDomingoSalida.text.toString(),
            domingoHoras = binding.etDomingoHoras.text.toString().toFloatOrNull() ?: 4f,

            toleranciaEntradaMin = binding.sliderToleranciaEntrada.value.toInt(),
            toleranciaSalidaMin = binding.sliderToleranciaSalida.value.toInt(),
            inicioJornadaNocturna = binding.tvInicioNocturna.text.toString(),
            finJornadaNocturna = binding.tvFinNocturna.text.toString(),
            usarHuella = binding.switchHuella.isChecked,
            usarPin = binding.switchPin.isChecked,
            notificarEntrada = binding.switchNotifEntrada.isChecked,
            notificarSalida = binding.switchNotifSalida.isChecked,
            minutosAntesNotificacion = binding.sliderMinutosNotif.value.toInt()
        )
        viewModel.guardarConfiguracion(config)
        Toast.makeText(context, "✅ Configuración guardada", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

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
        binding.etLunesHoras.setText(c.lunesHoras.toString())

        binding.switchMartes.isChecked = c.trabajaMartes
        binding.etMartesHoras.setText(c.martesHoras.toString())

        binding.switchMiercoles.isChecked = c.trabajaMiercoles
        binding.etMiercolesHoras.setText(c.miercolesHoras.toString())

        binding.switchJueves.isChecked = c.trabajaJueves
        binding.etJuevesHoras.setText(c.juevesHoras.toString())

        binding.switchViernes.isChecked = c.trabajaViernes
        binding.etViernesHoras.setText(c.viernesHoras.toString())

        binding.switchSabado.isChecked = c.trabajaSabado
        binding.etSabadoHoras.setText(c.sabadoHoras.toString())

        binding.switchDomingo.isChecked = c.trabajaDomingo
        binding.etDomingoHoras.setText(c.domingoHoras.toString())

        // Tolerancias

        // Nocturna
        binding.tvInicioNocturna.text = c.inicioJornadaNocturna
        binding.tvFinNocturna.text = c.finJornadaNocturna

        // Biométrico
        binding.switchHuella.isChecked = c.usarHuella
        binding.switchPin.isChecked = c.usarPin

        // Notificaciones
        binding.sliderMinutosNotif.value = c.minutosAntesNotificacion.toFloat()
        binding.tvMinutosNotifValor.text = "${c.minutosAntesNotificacion} min antes"
    }

    private fun configurarListeners() {
        binding.switchJornadaPersonalizada.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutHorasPersonalizadas.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Toggles de días — mostrar/ocultar horario de ese día

        // Time pickers por día

        // Nocturna
        binding.btnPickerInicioNocturna.setOnClickListener { timePicker(configActual.inicioJornadaNocturna) { binding.tvInicioNocturna.text = it } }
        binding.btnPickerFinNocturna.setOnClickListener { timePicker(configActual.finJornadaNocturna) { binding.tvFinNocturna.text = it } }

        // Sliders
        binding.sliderAlmuerzo.addOnChangeListener { _, v, _ -> binding.tvAlmuerzoValor.text = "${v.toInt()} min" }
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
            lunesHoras = binding.etLunesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaMartes = binding.switchMartes.isChecked,
            martesHoras = binding.etMartesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaMiercoles = binding.switchMiercoles.isChecked,
            miercolesHoras = binding.etMiercolesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaJueves = binding.switchJueves.isChecked,
            juevesHoras = binding.etJuevesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaViernes = binding.switchViernes.isChecked,
            viernesHoras = binding.etViernesHoras.text.toString().toFloatOrNull() ?: 8f,

            trabajaSabado = binding.switchSabado.isChecked,
            sabadoHoras = binding.etSabadoHoras.text.toString().toFloatOrNull() ?: 4f,

            trabajaDomingo = binding.switchDomingo.isChecked,
            domingoHoras = binding.etDomingoHoras.text.toString().toFloatOrNull() ?: 4f,

            inicioJornadaNocturna = binding.tvInicioNocturna.text.toString(),
            finJornadaNocturna = binding.tvFinNocturna.text.toString(),
            usarHuella = binding.switchHuella.isChecked,
            usarPin = binding.switchPin.isChecked,
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

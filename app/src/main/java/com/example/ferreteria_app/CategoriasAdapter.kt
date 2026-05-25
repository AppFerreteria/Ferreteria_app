package com.example.ferreteria_app

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoriasAdapter(
    private var listaCategorias: List<String>,
    private val onCategoriaClick: (String) -> Unit // Canal de comunicación con la Actividad
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

    // Por defecto, la posición 0 ("Todos") estará seleccionada al iniciar
    private var posicionSeleccionada = 0

    class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoriaNombre: TextView = view.findViewById(R.id.tvCategoriaNombre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = listaCategorias[position]
        holder.tvCategoriaNombre.text = categoria

        // Lógica de estado visual (Seleccionado vs No Seleccionado)
        if (position == posicionSeleccionada) {
            holder.tvCategoriaNombre.setTextColor(Color.parseColor("#E65100")) // Naranja de la marca
            holder.tvCategoriaNombre.setTypeface(null, Typeface.BOLD)
        } else {
            holder.tvCategoriaNombre.setTextColor(Color.parseColor("#4A3B32")) // Marrón oscuro normal
            holder.tvCategoriaNombre.setTypeface(null, Typeface.NORMAL)
        }

        // Listener de clic
        holder.itemView.setOnClickListener {
            val posicionAnterior = posicionSeleccionada
            posicionSeleccionada = holder.adapterPosition

            // Refrescar solo los dos ítems que cambiaron de estado para optimizar rendimiento
            notifyItemChanged(posicionAnterior)
            notifyItemChanged(posicionSeleccionada)

            // Enviar la categoría seleccionada a la Actividad
            onCategoriaClick(categoria)
        }
    }

    override fun getItemCount(): Int = listaCategorias.size

    fun actualizarLista(nuevaLista: List<String>) {
        listaCategorias = nuevaLista
        notifyDataSetChanged()
    }
}
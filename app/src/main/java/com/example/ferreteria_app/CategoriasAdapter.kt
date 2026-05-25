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
    private val onCategoriaClick: (String) -> Unit
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

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

        // Lógica de estado visual
        if (position == posicionSeleccionada) {
            holder.tvCategoriaNombre.setTextColor(Color.parseColor("#E65100"))
            holder.tvCategoriaNombre.setTypeface(null, Typeface.BOLD)
        } else {
            holder.tvCategoriaNombre.setTextColor(Color.parseColor("#4A3B32"))
            holder.tvCategoriaNombre.setTypeface(null, Typeface.NORMAL)
        }

        // Listener de clic
        holder.itemView.setOnClickListener {
            val posicionAnterior = posicionSeleccionada
            posicionSeleccionada = holder.adapterPosition

            notifyItemChanged(posicionAnterior)
            notifyItemChanged(posicionSeleccionada)

            onCategoriaClick(categoria)
        }
    }

    override fun getItemCount(): Int = listaCategorias.size

    fun actualizarLista(nuevaLista: List<String>) {
        listaCategorias = nuevaLista
        notifyDataSetChanged()
    }
}
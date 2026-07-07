package com.example.ferreteria_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade

class CarritoAdapter(
    private var lista: List<CarritoItem>,
    private val onCambiarCantidad: (CarritoItem, Int) -> Unit,
    private val onEliminar: (CarritoItem) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivImagenProductoCarrito)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProductoCarrito)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioProductoCarrito)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadItem)
        val btnRestar: TextView = view.findViewById(R.id.btnRestar)
        val btnSumar: TextView = view.findViewById(R.id.btnSumar)
        val ivEliminar: ImageView = view.findViewById(R.id.ivEliminarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = holder.itemView.context.getString(R.string.formato_precio, item.precio * item.cantidad)
        holder.tvCantidad.text = item.cantidad.toString()
        if (item.imagenUrl.isNotEmpty()) {
            holder.ivImagen.load(item.imagenUrl) { crossfade(true) }
        } else {
            holder.ivImagen.setImageResource(R.drawable.ic_martillo)
        }

        holder.btnRestar.setOnClickListener {
            if (item.cantidad > 1) {
                onCambiarCantidad(item, item.cantidad - 1)
            }
        }

        holder.btnSumar.setOnClickListener {
            onCambiarCantidad(item, item.cantidad + 1)
        }

        holder.ivEliminar.setOnClickListener {
            onEliminar(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<CarritoItem>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}
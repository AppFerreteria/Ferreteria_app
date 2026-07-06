package com.example.ferreteria_app

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class PedidoAdapter(
    private var lista: List<Pedido>,
    private val onSeguirPedido: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumeroPedido: TextView = view.findViewById(R.id.tvNumeroPedido)
        val tvFechaPedido: TextView = view.findViewById(R.id.tvFechaPedido)
        val tvBadgeEstado: TextView = view.findViewById(R.id.tvBadgeEstado)
        val tvDescripcionItems: TextView = view.findViewById(R.id.tvDescripcionItems)
        val tvTotalPedido: TextView = view.findViewById(R.id.tvTotalPedido)
        val btnSeguirPedido: MaterialButton = view.findViewById(R.id.btnSeguirPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = lista[position]
        val context = holder.itemView.context

        holder.tvNumeroPedido.text = context.getString(R.string.label_numero_pedido, pedido.numeroPedido)
        holder.tvFechaPedido.text = formatearFecha(pedido.fecha)
        holder.tvDescripcionItems.text = pedido.descripcionItems
        holder.tvTotalPedido.text = context.getString(R.string.formato_precio, pedido.total)

        val (textoBadge, colorFondo, colorTexto) = when (pedido.estado) {
            EstadoPedido.PENDIENTE -> Triple(
                context.getString(R.string.badge_pendiente),
                R.color.bg_badge_pendiente,
                R.color.text_badge_pendiente
            )
            EstadoPedido.PREPARACION -> Triple(
                context.getString(R.string.badge_preparacion),
                R.color.bg_badge_preparacion,
                R.color.text_badge_preparacion
            )
            EstadoPedido.EN_CAMINO -> Triple(
                context.getString(R.string.badge_en_camino),
                R.color.bg_badge_en_camino,
                R.color.text_badge_en_camino
            )
            else -> Triple(
                context.getString(R.string.badge_entregado),
                R.color.bg_badge_entregado,
                R.color.text_badge_entregado
            )
        }

        holder.tvBadgeEstado.text = textoBadge
        holder.tvBadgeEstado.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorFondo))
        holder.tvBadgeEstado.setTextColor(ContextCompat.getColor(context, colorTexto))

        if (pedido.estado == EstadoPedido.ENTREGADO) {
            holder.btnSeguirPedido.visibility = View.GONE
        } else {
            holder.btnSeguirPedido.visibility = View.VISIBLE
            holder.btnSeguirPedido.setOnClickListener { onSeguirPedido(pedido) }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Pedido>) {
        val diffCallback = PedidoDiffCallback(lista, nuevaLista)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.lista = nuevaLista
        diffResult.dispatchUpdatesTo(this)
    }

    private fun formatearFecha(fechaMillis: Long): String {
        // Usar Builder en lugar del constructor deprecated
        val locale = Locale.Builder()
            .setLanguage("es")
            .setRegion("PE")
            .build()
        val formato = SimpleDateFormat("dd 'de' MMMM, yyyy", locale)
        return formato.format(fechaMillis)
    }

    private class PedidoDiffCallback(
        private val oldList: List<Pedido>,
        private val newList: List<Pedido>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
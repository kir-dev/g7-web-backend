package hu.bme.sch.cmsch.component.debt

import com.fasterxml.jackson.annotation.JsonView
import hu.bme.sch.cmsch.admin.*
import hu.bme.sch.cmsch.component.EntityConfig
import hu.bme.sch.cmsch.dto.Edit
import hu.bme.sch.cmsch.dto.FullDetails
import hu.bme.sch.cmsch.dto.Preview
import hu.bme.sch.cmsch.model.ManagedEntity
import hu.bme.sch.cmsch.service.StaffPermissions
import org.hibernate.Hibernate
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.core.env.Environment
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

enum class ProductType {
    MERCH,
    FOOD,
    OTHER
}

@Entity
@Table(name="products")
@ConditionalOnBean(DebtComponent::class)
data class ProductEntity(
    @Id
    @GeneratedValue
    @Column(nullable = false)
    @field:JsonView(value = [ Edit::class ])
    @property:GenerateInput(type = INPUT_TYPE_HIDDEN, visible = true, ignore = true)
    @property:GenerateOverview(renderer = OVERVIEW_TYPE_ID, columnName = "ID", order = -1)
    override var id: Int = 0,

    @Column(nullable = false)
    @field:JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @property:GenerateInput(maxLength = 64, order = 1, label = "Név")
    @property:GenerateOverview(columnName = "Név", order = 1)
    @property:ImportFormat(ignore = false, columnId = 0)
    var name: String = "",

    @Column(nullable = false)
    @field:JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @property:GenerateInput(type = INPUT_TYPE_NUMBER, order = 2, label = "Ár", min = 1, defaultValue = "100", note = "JMF-ben természetesen")
    @property:GenerateOverview(columnName = "Ár", order = 1, centered = true)
    @property:ImportFormat(ignore = false, columnId = 1, type = IMPORT_INT)
    var price: Int = 0,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @field:JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @property:GenerateInput(type = INPUT_TYPE_BLOCK_SELECT, order = 3, label = "Típus", source = [ "MERCH", "FOOD", "OTHER" ])
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 2, type = IMPORT_ENUM, enumSource = ProductType::class)
    var type: ProductType = ProductType.OTHER,

    @Column(nullable = false, columnDefinition = "TEXT")
    @field:JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @property:GenerateInput(type = INPUT_TYPE_BLOCK_TEXT, order = 4, label = "Termék leírása")
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 3)
    var description: String = "",

    @Column(nullable = false)
    @field:JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @property:GenerateInput(type = INPUT_TYPE_FILE, order = 5, label = "Kép a termékről")
    @property:GenerateOverview(visible = false)
    var imageUrl: String = "",

    @Column(nullable = false)
    @field:JsonView(value = [ Edit::class, FullDetails::class ])
    @property:GenerateInput(type = INPUT_TYPE_SWITCH, order = 6, label = "Elérhető")
    @property:GenerateOverview(columnName = "Elérhető", order = 2, centered = true, renderer = OVERVIEW_TYPE_BOOLEAN)
    @property:ImportFormat(ignore = false, columnId = 4, type = IMPORT_BOOLEAN)
    var available: Boolean = false,

    @Column(nullable = false)
    @field:JsonView(value = [ Edit::class, FullDetails::class ])
    @property:GenerateInput(type = INPUT_TYPE_SWITCH, order = 7, label = "Látható")
    @property:GenerateOverview(columnName = "Látható", order = 3, centered = true, renderer = OVERVIEW_TYPE_BOOLEAN)
    @property:ImportFormat(ignore = false, columnId = 5, type = IMPORT_BOOLEAN)
    var visible: Boolean = false,

    @ColumnDefault("'payments'")
    @Column(nullable = false, length = 255)
    @field:JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @property:GenerateInput(order = 8, label = "React Icons - Fontawesome 5 ikon név",
        note = "Innen kell kimásolni a nevét az ikonnak:\n" +
                "https://react-icons.github.io/react-icons/icons/fa\n" +
                "Ajánlott ikonok: FaHamburger, FaHotdog, FaTshirt, FaPizzaSlice, FaCocktail, FaMoneyBill, FaUtensils, FaUtensilSpoon, FaVial, FaBox, FaBeer, FaHandMiddleFinger, FaShapes, FaHeart, FaFrog")
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 6)
    var materialIcon: String = "payments"

): ManagedEntity {

    override fun getEntityConfig(env: Environment) = EntityConfig(
        name = "Product",
        view = "control/products",
        showPermission = StaffPermissions.PERMISSION_SHOW_PRODUCTS
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ProductEntity

        return id != 0 && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }
}

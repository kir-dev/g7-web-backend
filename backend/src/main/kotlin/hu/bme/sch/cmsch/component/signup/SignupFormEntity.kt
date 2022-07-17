package hu.bme.sch.cmsch.component.signup

import com.fasterxml.jackson.annotation.JsonView
import hu.bme.sch.cmsch.admin.*
import hu.bme.sch.cmsch.dto.Edit
import hu.bme.sch.cmsch.dto.FullDetails
import hu.bme.sch.cmsch.dto.Preview
import hu.bme.sch.cmsch.model.ManagedEntity
import hu.bme.sch.cmsch.model.RoleType
import org.hibernate.Hibernate
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import javax.persistence.*

@Entity
@Table(name="signupForms")
@ConditionalOnBean(SignupComponent::class)
data class SignupFormEntity(
    @Id
    @GeneratedValue
    @JsonView(value = [ Edit::class ])
    @Column(nullable = false)
    @property:GenerateInput(type = INPUT_TYPE_HIDDEN, visible = true, ignore = true)
    @property:GenerateOverview(visible = false)
    override var id: Int = 0,

    @JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @Column(nullable = false)
    @property:GenerateInput(maxLength = 128, order = 1, label = "Űrlap címe")
    @property:GenerateOverview(columnName = "Cím", order = 1)
    @property:ImportFormat(ignore = false, columnId = 0)
    var name: String = "",

    @Lob
    @JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @Column(nullable = false)
    @property:GenerateInput(maxLength = 64, order = 2, label = "Url",
        note = "Csupa nem ékezetes kisbetű és kötőjel megegengedett", interpreter = INTERPRETER_PATH)
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 1)
    var url: String = "",

    @JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @Column(nullable = false)
    @property:GenerateInput(maxLength = 128, order = 3, label = "Menüben megjelenő neve",
        note = "Csak akkor szükséges ha menüből lesz megnyitható")
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 2)
    var menuName: String = "",

    @Lob
    @JsonView(value = [ Edit::class, FullDetails::class ])
    @Column(nullable = false)
    @property:GenerateInput(type = INPUT_TYPE_FORM_EDITOR, order = 4, label = "Kitöltendő űrlap", defaultValue = "[]")
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 3, type = IMPORT_LOB)
    var formJson: String = "[]",

    @Column(nullable = false)
    @JsonView(value = [ Edit::class ])
    @property:GenerateInput(type = INPUT_TYPE_BLOCK_SELECT, order = 5,
        label = "Minimum rang a megtekintéshez",
        note = "A ranggal rendelkező már megtekintheti (BASIC = belépett, STAFF = rendező)",
        source = [ "BASIC", "ATTENDEE", "STAFF", "ADMIN", "SUPERUSER" ])
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 4, type = IMPORT_ENUM, enumSource = RoleType::class)
    var minRole: RoleType = RoleType.BASIC,

    @Column(nullable = false)
    @JsonView(value = [ Edit::class ])
    @property:GenerateInput(type = INPUT_TYPE_BLOCK_SELECT, order = 6,
        label = "Maximum rang a megtekintéshez",
        note = "A ranggal rendelkező még megtekintheti (GUEST = kijelentkezett, BASIC = belépett, STAFF = rendező)",
        source = [ "BASIC", "ATTENDEE", "STAFF", "ADMIN", "SUPERUSER" ])
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 5, type = IMPORT_ENUM, enumSource = RoleType::class)
    var maxRole: RoleType = RoleType.SUPERUSER,

    @JsonView(value = [ Edit::class ])
    @Column(nullable = false)
    @property:GenerateInput(maxLength = 128, order = 7, label = "Átirányítási URL",
        note = "Ha nincs ilyen, akkor hagyd üresen. BME Jegy integrációhoz kell.")
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 6)
    var redirectUrl: String = "",

    @JsonView(value = [ Edit::class ])
    @Column(nullable = false)
    @property:GenerateInput(type = INPUT_TYPE_SWITCH, order = 8, label = "ATTENDEE jog automatikusan",
        note = "Automatikus ATTENDEE jog adása sikeres kitöltésért")
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 7, type = IMPORT_BOOLEAN)
    var grantAttendeeRole: Boolean = false,

    @JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @Column(nullable = false)
    @property:GenerateInput(type = INPUT_TYPE_DATE, order = 9, label = "Kitölthető innentől", defaultValue = "0")
    @property:GenerateOverview(columnName = "Ettől", order = 2, renderer = OVERVIEW_TYPE_DATE)
    @property:ImportFormat(ignore = false, columnId = 8, type = IMPORT_LONG)
    var availableFrom: Long = 0,

    @JsonView(value = [ Edit::class, Preview::class, FullDetails::class ])
    @Column(nullable = false)
    @property:GenerateInput(type = INPUT_TYPE_DATE, order = 10, label = "Kitölthető eddig", defaultValue = "0")
    @property:GenerateOverview(columnName = "Eddig", order = 3, renderer = OVERVIEW_TYPE_DATE)
    @property:ImportFormat(ignore = false, columnId = 9, type = IMPORT_LONG)
    var availableUntil: Long = 0,

    @JsonView(value = [ Edit::class ])
    @Column(nullable = false)
    @property:GenerateInput(type = INPUT_TYPE_SWITCH, order = 11, label = "Kitölthető-e",
        note = "Ha be van kapcsolva és az idő intervallum is megfelel, akkor lehet beküldeni")
    @property:GenerateOverview(columnName = "Kitölthető", order = 4, centered = true, renderer = OVERVIEW_TYPE_BOOLEAN)
    @property:ImportFormat(ignore = false, columnId = 10, type = IMPORT_BOOLEAN)
    var open: Boolean = false,

    @Column(nullable = false)
    @JsonView(value = [ Edit::class ])
    @property:GenerateInput(type = INPUT_TYPE_NUMBER, order = 12, label = "Maximum kitöltés",
        note = "Ennyi ember töltheti ki maximum. (-1 = végtelen)", min = -1, defaultValue = "0")
    @property:GenerateOverview(visible = false)
    @property:ImportFormat(ignore = false, columnId = 11, type = IMPORT_INT)
    var submissionLimit: Int = 0,

) : ManagedEntity {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SignupFormEntity

        return id != 0 && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }

}

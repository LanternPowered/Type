rootProject.name = "type"

listOf("jtype", "ktype").forEach { name ->
  include(name)
  project(":$name").name = name
}

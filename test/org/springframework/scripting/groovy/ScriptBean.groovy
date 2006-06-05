import org.springframework.scripting.ScriptBean

class GroovyScriptBean implements ScriptBean {

    private int age

    int getAge() {
        return this.age
    }
    
    void setAge(int age) {
        this.age = age
    }

    @Property String name
}

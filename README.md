### Pool de conexão Oracle com Spring

# 1. Pooling de conexão
Agora temos o banco de dados pronto para conexões de entrada. A seguir, vamos aprender algumas maneiras diferentes de fazer o pool de conexões no Spring.

### 2. HikariCP
A maneira mais fácil de fazer pooling de conexões com Spring é usando a autoconfiguração. A dependência spring-boot-starter-jdbc inclui HikariCP como a fonte de dados de pool preferencial. Portanto, se dermos uma olhada em nosso pom.xml, veremos:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

A dependência spring-boot-starter-data-jpa inclui a dependência spring-boot-starter-jdbc transitivamente para nós.

Agora só precisamos adicionar nossa configuração ao arquivo application.properties:

```
# OracleDB connection settings
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/NOME_DO_BANCO
spring.datasource.username=books
spring.datasource.password=books
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# HikariCP settings
spring.datasource.hikari.minimumIdle=5
spring.datasource.hikari.maximumPoolSize=20
spring.datasource.hikari.idleTimeout=30000
spring.datasource.hikari.maxLifetime=2000000
spring.datasource.hikari.connectionTimeout=30000
spring.datasource.hikari.poolName=HikariPoolBooks

# JPA settings
spring.jpa.database-platform=org.hibernate.dialect.Oracle12cDialect
spring.jpa.hibernate.use-new-id-generator-mappings=false
spring.jpa.hibernate.ddl-auto=create
```

Como você pode ver, temos três definições de configuração de seção diferentes:

- A seção de configurações de conexão OracleDB é onde configuramos as propriedades de conexão JDBC como sempre fazemos;
- A seção de configurações do HikariCP é onde configuramos o pool de conexão do HikariCP. Caso necessitemos de configuração avançada, devemos verificar a lista de propriedades de configuração do HikariCP;
- A seção de configurações JPA apresenta algumas configurações básicas para usar o Hibernate.

Isso é tudo de que precisamos. Não poderia ser mais fácil, não é?

# 3. Tomcat e Commons DBCP2 Connection Pooling
Spring recomenda o HikariCP por seu desempenho. Por outro lado, ele também suporta Tomcat e Commons DBCP2 em aplicativos autoconfigurados Spring Boot.

Ele tenta usar o HikariCP. Se não estiver disponível, tente usar o pool do Tomcat. Se nenhum deles estiver disponível, ele tenta usar Commons DBCP2.

Também podemos especificar o pool de conexão a ser usado. Nesse caso, precisamos apenas adicionar uma nova propriedade ao nosso arquivo application.properties:

```
spring.datasource.type=org.apache.tomcat.jdbc.pool.DataSource
```

Se precisarmos definir configurações específicas, temos disponíveis seus prefixos:

```
- spring.datasource.hikari.* para configuração do HikariCP;
- spring.datasource.tomcat.* para configuração de pool do Tomcat;
- spring.datasource.dbcp2.* para configuração do Commons DBC2.
```

E, na verdade, podemos definir spring.datasource.type como qualquer outra implementação de DataSource. Não é necessário ser nenhum dos três mencionados acima.

Mas, nesse caso, teremos apenas uma configuração básica pronta para usar. Haverá muitos casos em que precisaremos de algumas configurações avançadas. Vamos ver alguns deles.

# 4. Oracle Universal Connection Pooling
Se quisermos usar configurações avançadas, precisamos definir explicitamente o bean DataSource e definir as propriedades. Provavelmente, a maneira mais fácil de fazer isso é usando as anotações @Configuration e @Bean.

O Oracle Universal Connection Pool (UCP) para JDBC oferece uma implementação completa para o armazenamento em cache de conexões JDBC. Ele reutiliza as conexões em vez de criar novas. Ele também nos fornece um conjunto de propriedades para personalizar o comportamento do pool.

Se quisermos usar UCP, precisamos adicionar as seguintes dependências do Maven:

```
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
</dependency>
<dependency>
    <groupId>com.oracle.database.ha</groupId>
    <artifactId>ons</artifactId>
</dependency>
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ucp</artifactId>
</dependency>
```

Agora estamos prontos para declarar e configurar o pool de conexão UCP:

```
@Configuration
@Profile("oracle-ucp")
public class OracleUCPConfiguration {

    @Bean
    public DataSource dataSource() throws SQLException {
        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setUser("books");
        dataSource.setPassword("books");
        dataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        dataSource.setURL("jdbc:oracle:thin:@//localhost:1521/NOME_DO_BANCO");
        dataSource.setFastConnectionFailoverEnabled(true);
        dataSource.setInitialPoolSize(5);
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(10);
        return dataSource;
    }
}
```

No exemplo acima, personalizamos algumas propriedades do pool:

- setInitialPoolSize especifica o número de conexões disponíveis criadas depois que o pool é iniciado;
- setMinPoolSize especifica o número mínimo de conexões disponíveis e emprestadas que nosso pool está mantendo, e;
- setMaxPoolSize especifica o número máximo de conexões disponíveis e emprestadas que nosso pool está mantendo.

Se precisarmos adicionar mais propriedades de configuração, devemos verificar o PoolDataSource JavaDoc ou o guia do desenvolvedor.

Versões mais antigas do Oracle
Para versões anteriores a 11.2, como Oracle 9i ou 10g, devemos criar um OracleDataSource em vez de usar o Universal Connection Pooling da Oracle.

Em nossa instância OracleDataSource, ativamos o cache de conexão via setConnectionCachingEnabled:

```
@Configuration
@Profile("oracle")
public class OracleConfiguration {
    @Bean
    public DataSource dataSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setUser("books");
        dataSource.setPassword("books");
        dataSource.setURL("jdbc:oracle:thin:@//localhost:1521/NOME_DO_BANCO");
        dataSource.setFastConnectionFailoverEnabled(true);
        dataSource.setImplicitCachingEnabled(true);
        dataSource.setConnectionCachingEnabled(true);
        return dataSource;
    }
}
```

No exemplo acima, estávamos criando o OracleDataSource para pool de conexão e configuramos alguns parâmetros. Podemos verificar todos os parâmetros configuráveis no OracleDataSource JavaDoc.

# 5. Conclusão
Hoje em dia, configurar o pool de conexão de banco de dados Oracle usando Spring é um pedaço de bolo.

Vimos como fazer isso usando a configuração automática e de forma programática. Embora o Spring recomende o uso do HikariCP, outras opções estão disponíveis. Devemos ter cuidado e escolher a implementação certa para nossas necessidades atuais.
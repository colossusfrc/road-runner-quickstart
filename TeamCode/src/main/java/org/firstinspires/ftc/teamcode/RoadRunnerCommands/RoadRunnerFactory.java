package org.firstinspires.ftc.teamcode.RoadRunnerCommands;

import static org.firstinspires.ftc.teamcode.Constants.Instances.mecanumDrive;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;

import java.util.Optional;

public class RoadRunnerFactory{
    /**
    * O objetivo dessa classe é resolver a descontinuidade que a abordagem anterior geraria.
    * Essa classe possui um conjunto de métodos estáticos  que encapsulam as actions
    * utilizadas pela RoadRunner. Esses métodos sobrepõem initialize em uma classe anônima.
    * O comportamento partilhado por todos é implementado na superclasse: alocamos o getBase()
    * na instãncia. Esse getBase() vai recuperar a última posição do chassi e vai criar o
    * ActionBuilder a partir dela (agora, as trajetórias serão feitas com base na posição atual
    * do robõ (os movimetnos lineares efetuarão deslocamentos, os movimentos posicionais
    * gerarão uma linha entre a psoição atual do robô e o alvo).
    * Nessa classe, os comportamentos são manipulados a partir dos mé\todos esoclhidos.
    * Após a escolha, a instância será sobreposta com o próprio valor acrescido da action
    * abstraída.
    * Isso deve gerar o seguinte comportamento: Todas as vezes que o initialize() for executado,
    * a instância será sobreacarregada com o valor base. Esse valor base recuperará a posição
    * atual do robõ para o construtor da classe MecanumDrive, repassando a própria posição
    * do robõ como argumento para a posição inicial. Como a classe CommandRUnner é abstrata,
    * só podemos recorrer à classe de fábrica para gerar objetos a ela relacionados.
    * A recorrência aos mé\todos deverá sobreacarregar o objeto analisado no comando com o
    * valor base, acrescido de uma action encapsulada por um RoadRunnerAdministrator.
    * Esse objeto, agora, será finalmente útil para a classe de administração.
     */
    public static RoadRunnerAdministrator lineToX(double x){
        return new RoadRunnerAdministrator(mecanumDrive){
            @Override
            protected void initialize() {
                super.initialize();
                instance = Optional.of(instance.get().lineToX(x));
            }
        };
    }
    public static RoadRunnerAdministrator lineToY(double y){
        return new RoadRunnerAdministrator(mecanumDrive){
            @Override
            protected void initialize() {
                super.initialize();
                instance = Optional.of(instance.get().lineToY(y));
            }
        };
    }
    public static RoadRunnerAdministrator splineToLinearHeading(Pose2d pose, Rotation2d tangent){
        return new RoadRunnerAdministrator(mecanumDrive){
            @Override
            protected void initialize() {
                super.initialize();
                instance = Optional.of(instance.get().splineToLinearHeading(pose, tangent));
            }
        };
    }
}

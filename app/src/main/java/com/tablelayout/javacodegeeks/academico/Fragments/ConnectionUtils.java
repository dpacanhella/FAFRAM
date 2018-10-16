package com.tablelayout.javacodegeeks.academico.Fragments;

class ConnectionUtils {

  private static final String BASE = "http://45.55.209.136:8082/";
  private static final String POST_ALUNO = "alunos/aplicar-presenca";
  private static final String TODOS_PARTICIPANTES = "alunos/todos-participantes";

  public static String postPresenca() {
    return BASE + POST_ALUNO;
  }

  public static String getParticipantes() {
    return BASE + TODOS_PARTICIPANTES;
  }
}

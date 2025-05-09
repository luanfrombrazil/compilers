clear 

#SOMENTE PARA APAGAR OS .CLASS DESNECESSÁRIOS QUE SÃO CRIADOS E INUTILIZADOS CONFORME O CODIGO VAI SENDO MODIFICADO
cd out
rm *
cd .. 

rm output.txt
javac -d out Main.java



#PARA TESTES COM O SCANNER NO LUGAR DO MANIPULADOR DE ARQUIVO
cd out
java Main #>> ../output.txt #< ../util/input
cd ..

echo -e "\033[1;35mEXECUÇÃO FINALIZADA. EXECUTADO VIA \033[1;32mrun.sh.\033[0m"
# Chequeamos que nos llegue el directorio donde quedan los paquetes, el host de la maquina destino y el profile a desplegar
if (( $# != 3 ))
then
  echo "Debe indicar en el siguiente orden, el directorio donde estan los paquetes, el host de la maquina destinto y el perfil que se esta construyendo (local, desa, qa, uat, prep, prod)"
  exit 1
fi

# En la carpeta $1, tenemos los archivos *.jar, los archivos *.service y los archivos *.sh

# Reemplazamos el perfil que estamos construyendo en los archivos de especificacion de servicio
sed -i "s/__PROFILE__/$3/g" "$1/*.service"

# Realizamos una copia destino de los artefactos
scp $1/* "$2:/opt/pdi"
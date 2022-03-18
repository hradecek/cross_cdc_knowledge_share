# Kafka
KAFKA_BROKER="localhost:9092"
alias dockerc="docker-compose"
alias kcatc="kcat -b "${KAFKA_BROKER}" -C"
alias kcatp="kcat -b "${KAFKA_BROKER}" -P"
alias kcat-topics="kcat -b "${KAFKA_BROKER}" -L | grep topic"

function kcatG {
  kcat -b "$KAFKA_BROKER" -G "${1}" "${2}" ${@:3}
}

function kcatp-echo {
  echo "${1}" | kcatp ${@:2}
}

function kafka-topic-create {
  dockerc exec kafka bash -c "/bin/kafka-topics --bootstrap-server kafka:9092 --create --topic ${1} --partitions ${2:-1}"
}

# Docker
function dockerc-rm {
  dockerc rm -fv
}

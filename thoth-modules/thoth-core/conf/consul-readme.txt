docker run -d -p 8500:8500 -v consul_data:/consul/data consul agent -node-id=$(uuidgen | awk '{print tolower($0)}') -bootstrap -server -client=0.0.0.0 -ui

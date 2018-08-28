#include <string>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/property_tree/info_parser.hpp>

namespace ndn{
namespace ndncert{

typedef boost::property_tree::ptree JsonSection;

JsonSection getConfig(){
    std::string config =
        "{ "
        "  \"ca-list\": "
        "  [ "
        "    { "
        "        \"ca-prefix\": \"/ndn/edu/ucla/CA\", "
        "        \"ca-info\": \"NDN Testbed CA\", "
        "        \"certificate\": \"Bv0C7wcuCANuZG4IA2VkdQgEdWNsYQgDS0VZCAiXldTPPPi2FggCTkEICf0AAAFk2LPK/hQJGAECGQQANu6AFf0BJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALZRarQu+40IqPztowdEOPC9FcZ0zcFLiax7np9bU8Dr9WbdlIhX7u43boi6MfhbNTcJPsGeNRDiSb1j7xFMHqVoZ9u5UgTcrAIMQnm8beMXYW7rJm/Yih0r9ld0yTrhhG1izeugGjWTP1HYiT0BHMZ1P66UwTL7njRjGvked0TyRQ+MddFgaHPnkFOmdKtD52+vk9MofgS8MiRvKVN8ttnuthkWwa8CCQaPNPS5sGXB4cKZofuFnfoSvYSQdXUeXfpHZ40hJsQqNuTgpu9aSKZ2jH5R5fABDt4teEAfPQn8zmsofqaaYQMU1C812wp/N4qEcPQJhKgr0GtN0Ln19IECAwEAARb9AT0bAQMcFgcUCANuZG4IA0tFWQgIZZ1/pcWBEH39AP0m/QD+DzIwMTgwNzI1VDIyMjY1OP0A/w8yMDE5MDcyNlQyMjI2NTj9AQL0/QIAD/0CAQdhZHZpc29y/QICAP0CADP9AgEFZW1haWz9AgImL25kbi9lZHUvdWNsYUBvcGVyYXRvcnMubmFtZWQtZGF0YS5uZXT9AgBa/QIBCGZ1bGxuYW1l/QICSlRlc3QgQ2VydGlmaWNhdGUgZm9yIE5ldyBORE5DRVJUIGZvciBVbml2ZXJzaXR5IG9mIENhbGlmb3JuaWEsIExvcyBBbmdlbGVz/QIADf0CAQVncm91cP0CAgD9AgAP/QIBB2hvbWV1cmz9AgIA/QIAJP0CAQxvcmdhbml6YXRpb279AgIQTkROIFRlc3RiZWQgUm9vdBdHMEUCIAU+0iGKKGSL4mbU7Eo14Yr0DvTaiu+oAQBMDw96u/9GAiEA34mnZQaSPaSFaBjwK0IbtRXH6iSc7/EwI6hqcbntG1Y=\" "
        "    } "
        "  ] "
        "} ";
    std::istringstream ss(config);
    JsonSection json;
    boost::property_tree::json_parser::read_json(ss, json);
    return json;
}

} // namespace ndncert
} // namespace ndn


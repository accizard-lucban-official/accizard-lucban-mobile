package com.example.accizardlucban;

import java.util.ArrayList;
import java.util.List;

public class SafetyTipsContent {
    
    public static List<SafetyTipPage> createRoadSafetyPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Road Accidents";
        page1.subtitle = "in the Philippines";
        page1.content = "Road crashes are a major public health issue in the Philippines, with a growing trend of fatalities and injuries over the past decade. Statistics show that thousands of Filipinos lose their lives each year, with young adults and vulnerable road users being the most affected.";
        page1.bulletPoints = new String[]{
            "In 2023, the Philippine Statistics Authority (PSA) reported 13,125 deaths from land transport accidents, the highest recorded in over a decade.",
            "This represents an upward trend, with deaths increasing by 39% between 2011 and 2021.",
            "Road traffic injuries are the leading cause of death for Filipinos aged 15–29. Young adults aged 20–24 consistently have the highest number of road crash deaths, followed by those aged 25–29.",
            "Vulnerable road users, such as pedestrians, cyclists, and motorcyclists, make up a significant portion of fatalities. In fact, motorcycles are the vehicle type most frequently involved in crashes."
        };
        page1.imageResource = R.drawable.road_safety_poster;
        pages.add(page1);

        // Page 2: Major Contributing Factors
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of road accidents can help prevent them:";
        page2.bulletPoints = new String[]{
                "Human error: A significant majority of road accidents (over 87%) are caused by reckless driving, which includes speeding, improper turning, drunk driving, and using mobile phones while driving.",
                "Drunk driving: Driving under the influence of alcohol was identified as the highest reported contributing factor in a 2010–2019 study, with alcohol use significantly associated with more severe injuries.",
                "Low use of safety equipment: The same study revealed low compliance with the use of safety equipment like helmets and seatbelts.",
                "Poor road infrastructure and vehicle condition: Other contributing factors include damaged roads and poorly maintained vehicles."
        };
        page2.imageResource = R.drawable.road_safety_poster;
        pages.add(page2);

        // Page 3: Safety Tips for Pedestrians
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "For Pedestrians and Passengers";
        page3.content = "Follow these essential safety tips to protect yourself and others on the road:";
        page3.bulletPoints = new String[]{
                "Use sidewalks and crosswalks. Utilize designated pedestrian areas or walk facing oncoming traffic if no sidewalk is available.",
                "Look both ways. Before crossing, look left, right, then left again, and don't assume drivers will stop.",
                "Avoid distractions. Don't use phones or headphones when crossing streets or walking near busy roads.",
                "Be visible at night. Wear bright or reflective clothing when walking in the dark or poor weather.",
                "Keep your seatbelt on. Always fasten your seatbelt, even in the back seat.",
                "Hold on in PUVs. Wait for the vehicle to stop completely before boarding or alighting. Hold onto railings and avoid hanging onto the sides of jeepneys.",
                "Do not distract the driver. Avoid actions or conversations that could distract the driver."
        };
        page3.imageResource = R.drawable.road_safety_poster;
        pages.add(page3);

        // Page 4: Safety Tips for Drivers
        SafetyTipPage page4 = new SafetyTipPage();
        page4.title = "Safety Tips";
        page4.subtitle = "For Drivers";
        page4.content = "As a driver, you have a responsibility to keep yourself and others safe on the road:";
        page4.bulletPoints = new String[]{
                "Be aware and drive defensively. Be aware of unpredictable traffic, such as sudden stops or jaywalking, and anticipate mistakes from others.",
                "Wear a seatbelt. Philippine law requires seatbelt use for both drivers and passengers in the front and back seats, as it is highly effective in reducing injuries in a collision.",
                "Avoid distractions. It is illegal to use a mobile device while driving, even at a red light.",
                "Obey all traffic signs and signals. Follow traffic lights and signs for orderly traffic flow and do not run red lights.",
                "Stay sober and alert. Driving under the influence of alcohol or drugs is illegal. Never drive when intoxicated or tired, as this impairs judgment and reaction time.",
                "Observe speed limits. Speeding is a major cause of accidents, so pay attention to speed limit signs, which vary by road type. Drive slower in adverse conditions.",
                "Keep a safe distance. Maintain a safe following distance, especially in heavy traffic, to react to sudden stops."
        };
        page4.imageResource = R.drawable.road_safety_poster;
        pages.add(page4);
        
        // Page 5: References
        SafetyTipPage page5 = new SafetyTipPage();
        page5.title = "References";
        page5.subtitle = "";
        page5.content = "";
        page5.bulletPoints = new String[]{
            "Calonzo, A. (2024, May 18). DOH: 12,000 killed on roads each year. Inquirer.net. https://newsinfo.inquirer.net/1941842/doh-12000-killed-on-roads-each-year",
            "Global Dominion Financing Inc. (n.d.). Philippine rules of the road: Essential driving & safety tips. Retrieved September 14, 2025, from https://gdfi.com.ph/philippine-rules-of-the-road-essential-driving-safety-tips/",
            "Magallanes, M. R., & Sison, J. L. (2020). Analysis of transport and vehicular crash cases using the online national electronic injury surveillance system (ONEISS), Philippines, January 2010-June 2019. Acta Medica Philippina, 54(5), 517-523.",
            "Makati Medical Center. (2023, July 28). The road safety guide. https://www.makatimed.net.ph/blogs/road-safety-guide/",
            "Philippine Statistics Authority. (2025, May 7). Content | Philippine Statistics Authority. https://psa.gov.ph/statistics/vital-statistics/node/1684076211",
            "SAFC. (2025, May 16). Road safety tips in the Philippines you must know. https://safc.com.ph/top-12-road-safety-tips-every-filipino-driver-needs-to-know-in-2025/",
            "World Health Organization Philippines. (2023, May 31). Department of Transportation, World Health Organization launch Philippine Road Safety Action Plan 2023-2028. https://www.who.int/philippines/news/detail/31-05-2023-department-of-transportation--world-health-organization-launch-philippine-road-safety-action-plan-2023-2028"
        };
        page5.imageResource = R.drawable.road_safety_poster;
        pages.add(page5);
        
        return pages;
    }
    
    public static List<SafetyTipPage> createFireSafetyPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Volcanic Activity";
        page1.subtitle = "in the Philippines";
        page1.content = "Volcanic activity involves eruptions that release ash, lava, hot gases, and rock fragments into the environment—with both immediate and longer-term impacts on health, infrastructure, and ecosystems. Eruptions vary in style and intensity, from lava flows and gas emissions, to explosive pyroclastic flows, ash clouds, and mudflows (lahars). These events can affect areas close to the volcano as well as far‐downwind communities, especially when ash and gas are carried by wind.";
        page1.bulletPoints = new String[]{
            "Volcanic eruptions can cause burns, traumatic injuries, suffocation, eye and skin irritation, respiratory diseases, and death depending on proximity and exposure.",
            "Ash fall can damage roofs (sometimes collapse), contaminate water, disrupt transport, communications, and essential services.",
            "Volcanic gases (e.g. sulfur dioxide, hydrogen chloride, hydrogen fluoride) can lead to air pollution causing both acute symptoms (irritation of eyes, throat, breathing difficulty) and longer‐term health issues (bronchitis, chronic respiratory disease) especially among vulnerable people.",
            "Secondary hazards such as lahars (mudflows), landslides, floods, and damage due to roof collapse or debris flows may follow, especially when heavy rain or melting ice is involved."
        };
        page1.imageResource = R.drawable.volcano_safety_poster;
        pages.add(page1);
        
        // Page 2: Prevention Tips
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of volcanic activity can help prevent them:";
        page2.bulletPoints = new String[]{
            "Proximity to active volcanoes and level of eruptive activity: explosive eruptions, pyroclastic flows, and frequent eruptions increase risk.",
            "Wind patterns and weather: carry ash and gases to downwind areas, spreading the impact.",
            "Topography: steep slopes can lead to faster flow of pyroclastic materials, lahars, and make evacuation harder.",
            "Population density and infrastructure: densely populated areas near volcanoes, poorly constructed buildings, weak roofs are more vulnerable.",
            "Health vulnerabilities: existing respiratory or cardiovascular conditions, age (children, elderly), etc.",
        };
        page2.imageResource = R.drawable.volcano_safety_poster;
        pages.add(page2);
        
        // Page 3: Emergency Preparedness
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "for Individuals";
        page3.content = "Follow these essential safety tips to protect yourself in case of volcanic activity:";
        page3.bulletPoints = new String[]{
            "Monitor warnings and alerts issued by volcanological authorities.",
            "If ash fall is occurring or expected: stay indoors, close windows/doors, seal openings; use masks (e.g. N95) if going outside.",
            "Protect eyes and skin by wearing goggles, long sleeves, and long pants.",
            "Be prepared for secondary hazards: know evacuation routes in case of lahars or mudflows, avoid valleys or river channels below volcanoes during heavy rain.",
            "Keep a supply of clean water, food, and medical needs; prevent contamination of water supplies.",
            "After ash settles: avoid driving in dense ash, clear roofs safely to avoid collapse (but do so carefully), clean up ash with proper protective gear.",
        };
        page3.imageResource = R.drawable.volcano_safety_poster;
        pages.add(page3);

        // Page 4: References
        SafetyTipPage page4 = new SafetyTipPage();
        page4.title = "References";
        page4.subtitle = "";
        page4.content = "";
        page4.bulletPoints = new String[]{
                "British Geological Survey. (2012). Volcanic hazards. British Geological Survey. https://www.bgs.ac.uk/discovering-geology/earth-hazards/volcanoes/volcanic-hazards/",
                "Centers for Disease Control and Prevention. (2024, January 30). Health effects of volcanic air pollution. U.S. Department of Health & Human Services. https://www.cdc.gov/volcanoes/risk-factors/index.html",
                "Pan American Health Organization/World Health Organization. (n.d.). Volcanic eruptions – Regional impacts & health effects. Pan American Health Organization. https://www.paho.org/en/topics/volcanic-eruptions",
                "U.S. Geological Survey. (n.d.). Volcanic ash & gas impacts & mitigation. U.S. Department of the Interior. https://volcanoes.usgs.gov",
                "World Health Organization. (n.d.). Volcanic eruptions: Health topics. World Health Organization. https://www.who.int/health-topics/volcanic-eruptions",
        };
        page4.imageResource = R.drawable.volcano_safety_poster;
        pages.add(page4);

        return pages;
    }
    
    public static List<SafetyTipPage> createLandslideSafetyPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Landslides";
        page1.subtitle = "in the Philippines";
        page1.content = "Landslides occur when masses of soil, rock, or debris move downslope due to gravity, often rapidly, causing damage to property, infrastructure, and human lives. They can be triggered by natural events such as heavy rainfall, earthquakes, volcanic activity, or by human actions like deforestation, slope alteration, or improper land use. In the Philippines, landslides are especially frequent during the rainy season and in mountainous or steep terrain, affecting communities, displacing families, and causing injuries or fatalities.\n";
        page1.bulletPoints = new String[]{
            "Natural triggers include intense or prolonged rainfall, earthquakes, volcanic activity, ground saturation, and weathering of rocks.",
            "Human contributions include deforestation, removal of vegetation, unplanned construction on slopes, changing drainage, and slope grading without stabilization.",
            "Landslide types range from mudflows/debris flows (fast-moving, water-saturated), rockfalls, slides, spreads, and falls.",
            "Impacts: burial of homes, destruction of roads and infrastructure, disruption of utilities (water, electricity, communication), injuries (trauma, suffocation), displacement of populations, plus potential longer-term health risks."
        };
        page1.imageResource = R.drawable.landslide_safety_poster;
        pages.add(page1);
        
        // Page 2: Warning Signs
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of landslide can help prevent them:";
        page2.bulletPoints = new String[]{
            "Rainfall intensity/duration: when rain is heavy or prolonged, soil becomes saturated and unstable.",
            "Steep slopes, geologic weakness, weathered or fractured soil/rock.",
            "Earthquakes or volcanic activity destabilizing slopes.",
            "Human land-use practices: deforestation, poor drainage, slope alteration, building in hazard zones.",
            "Climate change effects: more intense rainfall, more extreme weather, and possibly longer wet seasons increase risk.",
        };
        page2.imageResource = R.drawable.landslide_safety_poster;
        pages.add(page2);
        
        // Page 3: Prevention Measures
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = " for Individuals";
        page3.content = "Follow these essential safety tips to protect yourself in case of landslide:";
        page3.bulletPoints = new String[]{
            "Identify if your area is slope-prone or has prior landslide history; stay alert to early warning signs (cracks in ground, tilting trees, unusual sounds).",
            "Avoid building or staying below steep slopes or at the foot of unstable terrain.",
            "Manage drainage: ensure water flows are controlled, gutters, drains, waterways are clear; avoid pooling water on slopes.",
            "During heavy rainfall, stay away from known landslide-hazard areas; evacuate if authorities issue warnings.",
            "Keep an emergency kit and a family plan: know evacuation routes, where to go, and have essential supplies ready.",
        };
        page3.imageResource = R.drawable.landslide_safety_poster;
        pages.add(page3);

        // Page 5: References
        SafetyTipPage page5 = new SafetyTipPage();
        page5.title = "References";
        page5.subtitle = "";
        page5.content = "";
        page5.bulletPoints = new String[]{
                "National Aeronautics and Space Administration (NASA). (n.d.). Landslide: introduction to landslide. PHIVOLCS-DOST. https://www.phivolcs.dost.gov.ph/index.php/landslide/introduction-to-landslide",
                "U.S. Geological Survey. (n.d.). What is a landslide and what causes one? https://www.usgs.gov/faqs/what-a-landslide-and-what-causes-one",
                "World Health Organization. (n.d.). Landslides. https://www.who.int/health-topics/landslides",
                "U.S. Geological Survey. (n.d.). Do human activities cause landslides? https://www.usgs.gov/faqs/do-human-activities-cause-landslides",
        };
        page5.imageResource = R.drawable.landslide_safety_poster;
        pages.add(page5);

        return pages;
    }
    
    public static List<SafetyTipPage> createEarthquakeSafetyPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Earthquake Overview";
        page1.subtitle = "in the Philippines";
        page1.content = "Earthquakes are sudden ground shaking events caused by the rapid release of energy in Earth's crust, often due to fault movements, volcanic activity, or tectonic plate interactions. They can vary in magnitude, depth, and location, leading to effects ranging from mild tremors to catastrophic destruction of buildings, infrastructure, and lives. In the Philippines, being on the Pacific Ring of Fire and crossed by active fault lines, frequent seismic activity places many communities at risk of serious damage, injury, and disruption of essential services.";
        page1.bulletPoints = new String[]{
            "Earthquake sources include tectonic faults, volcanic activity, and human-induced causes such as mining or reservoir induced seismicity.",
            "Damage from earthquakes often comes from collapsing structures, falling debris, ground rupture, liquefaction (where saturated soil loses strength), and aftershocks.",
            "Major health impacts include physical injuries (cuts, fractures, crush injuries), trauma from building collapse, as well as secondary effects: lack of access to medical care, water/sanitation breakdowns, disease risks, mental health stress. (Salazar, 2016; UNICEF, 2022)",
            "Philippines-specific impacts: in large quakes, many homes, hospitals, schools, and infrastructures get damaged; sometimes displacement of people; access to health facilities can be impeded due to roads being damaged or blocked. (World Bank report on Metro Manila's seismic resilience)"
        };
        page1.imageResource = R.drawable.earthquake_safety_poster;
        pages.add(page1);
        
        // Page 2: Before an Earthquake
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of earthquake can help prevent them:";
        page2.bulletPoints = new String[]{
            "Proximity to active faults and volcanoes, which generate tectonic or volcanic earthquakes.",
            "Building quality and construction standards: weak or non-engineered buildings are more likely to collapse.",
            "Soil type and topographical features (e.g., soft soils, slopes prone to landslide or liquefaction).",
            "Population density: more people in vulnerable zones (e.g. densely built zones, informal settlements).",
            "Lack of preparedness: insufficient structural retrofitting, low awareness, poor disaster planning and early warning systems.",
        };
        page2.imageResource =  R.drawable.earthquake_safety_poster;
        pages.add(page2);
        
        // Page 3: During an Earthquake
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "for Individuals";
        page3.content = "Follow these essential safety tips to protect yourself in case of earthquake:";
        page3.bulletPoints = new String[]{
            "Before an earthquake: secure heavy furniture, ensure building meets code if possible, prepare \"go-bag\" with essentials, know escape routes and safe spots (under sturdy tables, away from windows).",
            "During shaking: Drop, Cover, and Hold On — move away from windows, take shelter under a sturdy table or furniture, protect your head and neck.",
            "After the quake: check for injuries, assist others if safe, inspect for structural damage; if your home is unsafe, evacuate.",
            "Be cautious of aftershocks which may follow the main quake.",
            "Prepare emergency supplies: water, food, flashlight, first aid, clothing; have plans for communication if utilities are down.",
            "Keep updated with information from PHIVOLCS, local disaster authorities, and follow warnings.",
        };
        page3.imageResource =  R.drawable.earthquake_safety_poster;
        pages.add(page3);

        // Page 5: References
        SafetyTipPage page5 = new SafetyTipPage();
        page5.title = "References";
        page5.subtitle = "";
        page5.content = "";
        page5.bulletPoints = new String[]{
                "National Geographic Society. (n.d.). Landslide / what causes landslides. National Geographic Education. https://education.nationalgeographic.org/resource/landslide/#:~:text=A%20landslide%20is%20the%20movement,What%20Causes%20Landslides?",
                "PHIVOLCS-DOST. (n.d.). Introduction to earthquake. Philippine Institute of Volcanology and Seismology. https://www.phivolcs.dost.gov.ph/index.php/earthquake/introduction-to-earthquake",
                "Red Cross. (n.d.). How to prepare for earthquakes. American Red Cross. https://www.redcross.org/get-help/how-to-prepare-for-emergencies/types-of-emergencies/earthquake.html",
                "USGS. (n.d.). Science of earthquakes. U.S. Geological Survey. https://www.usgs.gov/programs/earthquake-hazards/science-earthquakes",
                "Metal Technology University (MTU). (n.d.). Earthquake take action: What to do before, during, and after an earthquake. MTU Seismology Community. https://www.mtu.edu/geo/community/seismology/learn/earthquake-take-action/#:~:text=What%20to%20Do%20Before%20an,yourself%20and%20others%20for%20injuries",
        };
        page5.imageResource =  R.drawable.earthquake_safety_poster;
        pages.add(page5);

        return pages;
    }
    
    public static List<SafetyTipPage> createFloodSafetyPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Flooding";
        page1.subtitle = "in the Philippines";
        page1.content = "Flooding is a frequent and destructive natural disaster in the Philippines and many parts of the world, caused by a variety of weather and environmental conditions. It can result from overflowing rivers, heavy rainfall (especially during storms or monsoons), storm surges, and failure of flood control structures. Depending on the type, floods may develop gradually or suddenly, affecting communities, infrastructure, and livelihoods.";
        page1.bulletPoints = new String[]{
            "There are several common types of floods: fluvial (river floods), pluvial (surface water/rainfall-driven floods), and coastal floods.",
            "Flash floods develop rapidly (within a few hours or less) after intense rainfall, especially in steep or mountainous areas, or when drainage is poor.",
            "River basin flooding or fluvial floods happen when rivers overflow their banks due to heavy upstream rainfall or snowmelt, inadequate capacity, or saturated soil.",
            "Storm surges and high tides can lead to coastal flooding, especially during tropical cyclones or when sea levels are elevated."
        };
        page1.imageResource = R.drawable.flood_safety_poster;
        pages.add(page1);
        
        // Page 2: Preparation and Prevention
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of earthquake can help prevent them:";
        page2.bulletPoints = new String[]{
            "Heavy or prolonged rainfall, especially during tropical cyclones, monsoon systems, or tail ends of cold fronts.",
            "Topography / terrain: steep slopes enable rapid runoff, while low-lying or floodplain areas can accumulate large volumes of floodwater.",
            "Inadequate drainage infrastructure, blocked waterways, or poorly maintained riverbanks and flood defences.",
            "Coastal factors such as storm surge, high tides, and sea level rise.",
            "Land use changes like deforestation, urbanization, and reduction of natural absorption areas, which decrease water infiltration and increase runoff.",
        };
        page2.imageResource = R.drawable.flood_safety_poster;
        pages.add(page2);
        
        // Page 3: During a Flood
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "for Individuals";
        page3.content = "When flooding occurs, follow these critical safety guidelines:";
        page3.bulletPoints = new String[]{
            "Stay informed — monitor flood warnings and forecasts from PAGASA or local authorities.",
            "Avoid driving or walking through floodwaters; even shallow water can be dangerous and fast-moving.",
            "Prepare a family emergency plan (evacuation routes, meeting points) and build an emergency kit with essentials.",
            "Keep drains, gutters, and stormwater paths clear of debris so water can flow freely.",
            "Elevate electrical outlets/appliances in flood-prone areas and move valuables to higher ground.",
            "During storms or high tide warnings, if possible, avoid living in or moving to floodplain or coastal low areas without flood mitigation.",
        };
        page3.imageResource = R.drawable.flood_safety_poster;
        pages.add(page3);

        // Page 5: References
        SafetyTipPage page5 = new SafetyTipPage();
        page5.title = "References";
        page5.subtitle = "";
        page5.content = "";
        page5.bulletPoints = new String[]{
                "Zurich Resilience Solutions. (2025, April 17). Three common types of floods: fluvial, pluvial, and coastal floods. Zurich. https://www.zurich.com/knowledge/topics/flood-and-water-damage/three-common-types-of-flood",
                "Zurich North America. (2024, June 26). Pluvial flooding: Protecting your property from a growing risk. https://www.zurichna.com/knowledge/articles/2024/06/pluvial-flooding-protecting-your-property-from-a-growing-risk",
                "PAGASA-DOST. Floods. Philippine Atmospheric, Geophysical and Astronomical Services Administration. https://www.pagasa.dost.gov.ph/learning-tools/floods",
                "PAGASA-DOST. Flood Forecasting and Warning System for River Basins. https://www.pagasa.dost.gov.ph/information/flood-forecasting-and-warning-system-river-basins",
        };
        page5.imageResource = R.drawable.flood_safety_poster;
        pages.add(page5);

        return pages;
    }

    public static List<SafetyTipPage> createVolcanicSafetyPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Medical Emergencies";
        page1.subtitle = "in the Philippines";
        page1.content = "Medical emergencies are a critical global health concern, ranging from sudden illnesses to accident-related injuries that require immediate medical care. They can occur in everyday settings such as roads, homes, and mass gatherings, often placing heavy demands on healthcare systems and emergency responders.";
        page1.bulletPoints = new String[]{
            "Trauma is one of the most common causes, including fractures, contusions, sprains, and other accident-related injuries.",
            "Mass gatherings frequently lead to fainting, heat-related illnesses, and crowd-surge injuries.",
            "Road traffic crashes remain a leading global cause of medical emergencies, and are the top cause of death for individuals aged 5–29, with pedestrians, cyclists, and motorcyclists most affected (CDC, 2024).",
            "Hospital-based studies report open wounds, abrasions, fractures, and internal injuries as the most frequent cases, with pedestrians and vehicle occupants making up a significant portion (Rajeev et al., 2022)."
        };
        page1.imageResource = R.drawable.medical_emergency_safety_poster;
        pages.add(page1);
        
        // Page 2: Warning Signs and Monitoring
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of medical emergency can help prevent them:";
        page2.bulletPoints = new String[]{
            "Accidents and trauma: Vehicle collisions, falls, and blunt force trauma account for a significant portion of emergency cases.",
            "Mass gatherings: Large crowds increase the risk of stampedes, dehydration, fainting, and heat-related illnesses.",
            "Human behavior: Alcohol use, violence, and negligence (such as unsafe driving) often worsen the severity of emergencies.",
            "Environmental conditions: Heat, poor crowd control, and inadequate safety planning can trigger widespread medical incidents.",
            "Chronic and sudden illness: Cardiac arrests, strokes, and respiratory distress also account for a high number of emergency admissions.",
        };
        page2.imageResource = R.drawable.medical_emergency_safety_poster;
        pages.add(page2);
        
        // Page 3: Preparation and Evacuation
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "for Individuals";
        page3.content = "Follow these essential safety tips to protect yourself in case of medical emergency:";
        page3.bulletPoints = new String[]{
            "Stay alert in crowds. Be aware of exits during mass events and avoid overcrowded areas.",
            "Use protective equipment. Wear helmets, seatbelts, and protective gear to minimize injury risk during accidents.",
            "Avoid risky behaviors. Refrain from excessive alcohol use, reckless driving, or dangerous stunts.",
            "Hydrate and rest. Heat-related emergencies are common, so drink water regularly and avoid overexertion.",
            "Know basic first aid. Learning CPR and first aid techniques can save lives in emergencies.",
            "Seek help immediately. Do not delay medical attention for injuries, chest pain, difficulty breathing, or sudden severe illness.",
        };
        page3.imageResource = R.drawable.medical_emergency_safety_poster;
        pages.add(page3);

        // Page 5: References
        SafetyTipPage page5 = new SafetyTipPage();
        page5.title = "References";
        page5.subtitle = "";
        page5.content = "";
        page5.bulletPoints = new String[]{
                "Centers for Disease Control and Prevention. (2024). Global Road Safety. https://www.cdc.gov/transportation-safety/global/index.html",
                "Hutton, A., Ranse, J., & Verdonk, N. (2021). Characteristics of mass gatherings from a mass-gathering health perspective: A narrative review. Prehospital and Disaster Medicine, 36(3), 1–7. https://www.sciencedirect.com/science/article/pii/S2212420921003344",
                "Kraft Law. (n.d.). Common injuries in car accidents. https://www.kraftlaw.com/car-accidents/common-injuries-car-accidents",
                "PLOS Currents Disasters. (2017). Disasters at mass gatherings: Lessons learned. https://currents.plos.org/disasters/article/disasters-at-mass-gatherings-lessons-3wkcplftb6ss-5/",
                "Rajeev, A., et al. (2022). Epidemiology of road traffic accidents and related injuries in India: Hospital-based study. Journal of Emergencies, Trauma, and Shock, 15(2), 45–53. https://pmc.ncbi.nlm.nih.gov/articles/PMC9124333",
        };
        page5.imageResource = R.drawable.medical_emergency_safety_poster;
        pages.add(page5);

        return pages;
    }
    
    public static List<SafetyTipPage> createCivilDisturbancePages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Civil Disturbance";
        page1.subtitle = "in the Philippines";
        page1.content = "Civil disturbance (also called civil unrest, protest, riot, or civil disorder) refers to collective actions by groups — such as demonstrations, strikes, or riots — that disrupt public peace, order, or essential services. These events may start peacefully, but can escalate rapidly, often resulting in property damage, physical injuries, or even fatalities, especially when law enforcement responds forcefully or when crowd control is inadequate. The health, economic, and social consequences can be significant, especially for vulnerable populations.";
        page1.bulletPoints = new String[]{
            "Civil unrest is defined as sporadic but continued collective physical violence in a context of social or political instability, which may result in deaths, injury, and destruction. Peaceful demonstrations can escalate into violence under certain conditions.",
            "The types of civil disturbance range from blocking roads or sit-ins, to protests, counter-protests, and full-scale riots.",
            "Injury types include blunt trauma (from hitting, falling, trampling), lacerations, contusions, injuries from projectiles (rubber bullets, tear gas canisters), eye, head, chest trauma, and sometimes chemical irritation from crowd control agents.",
            "Beyond physical injury, affected populations often suffer psychological impacts: stress, anxiety, depression, post-traumatic stress disorder (PTSD), especially for those directly exposed or living nearby."
        };
        page1.imageResource = R.drawable.civil_disturbance_poster;
        pages.add(page1);
        
        // Page 2: Prevention and Awareness
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of civil and disturbance can help prevent them:";
        page2.bulletPoints = new String[]{
            "Political, social, or economic grievances: corruption, inequality, controversial policies, lack of political representation or unfair enforcement.",
            "Trigger events or tipping points: elections, controversial arrests, policy changes, police action that is perceived as unjust.",
            "Poor crowd control, absence or delay of mediation, or overreaction by security forces.",
            "Lack of preparedness of authorities and emergency services: no clear plan for medical response, evacuation, or safe zones.",
            "Media/social media amplification, misinformation, or emotional contagion can escalate tensions.",
        };
        page2.imageResource = R.drawable.civil_disturbance_poster;
        pages.add(page2);
        
        // Page 3: Avoiding Dangerous Areas
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "for Individuals";
        page3.content = "Follow these essential safety tips to protect yourself in case of civil disturbance:";
        page3.bulletPoints = new String[]{
            "Avoid areas of protest or unrest if possible. If you must be in or near them, have exit routes and safe spaces identified ahead of time.",
            "Wear protective clothing: sturdy shoes, long pants, long sleeves, and eye protection (goggles) in case of projectiles or tear gas.",
            "Keep a small emergency kit: water, mask (for breathing protection), first aid basics, flashlight.",
            "During unrest, stay low if there's tear gas (cover your mouth and nose), avoid conflict zones, avoid provoking or confronting demonstrators or law enforcement.",
            "Stay informed: follow official alerts or local news, monitor social media and trustworthy sources.",
            "After incidents, seek medical help for injuries—even if they seem mild; also consider mental health support for anxiety, trauma.",
        };
        page3.imageResource = R.drawable.civil_disturbance_poster;
        pages.add(page3);

        // Page 4: References
        SafetyTipPage page4 = new SafetyTipPage();
        page4.title = "References";
        page4.subtitle = "";
        page4.content = "";
        page4.bulletPoints = new String[]{
                "Disaster Risk Reduction (UNDRR). (n.d.). Civil Unrest: Understanding Disaster Risk SO0103. UNDRR. https://www.undrr.org/understanding-disaster-risk/terminology/hips/so0103",
                "New Jersey Office of Emergency Management. (2014). Mitigation 2014 – Section 5-14: Civil Unrest. https://nj.gov/njoem/programs/pdf/mitigation2014b/mit2014_section5-14.pdf",
                "Bui, A. L., et al. (2025). Protest-related injuries during the Capitol Hill Autonomous … Injury Prevention. https://injuryprevention.bmj.com/content/early/2025/01/01/ip-2024-045396",
                "University of Michigan Institute for Social Research. (2023, June 27). Exposure to civil violence increases risk for mental health disorders, even for non-combatants. https://isr.umich.edu/news-events/news-releases/exposure-to-civil-violence-increases-risk-for-mental-health-disorders/",
                "The EM Resident / EMRA. (2020). The Riot Trauma: What Injuries Should You Expect From Violent Protests. EM Resident. https://www.emra.org/emresident/article/riot-trauma",
        };
        page4.imageResource = R.drawable.civil_disturbance_poster;
        pages.add(page4);

        return pages;
    }
    
    public static List<SafetyTipPage> createArmedConflictPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Armed Conflict";
        page1.subtitle = "in the Philippines";
        page1.content = "Armed conflict refers to organized, sustained violent confrontation between state and/or non-state armed groups that disrupts governance, civilian life, and essential services. These conflicts cause immediate physical harm (deaths, injuries), force population displacement, and produce long-term health, economic, and social damage—including destroyed infrastructure, interrupted healthcare, food insecurity, and psychological trauma. Civilians, especially children, the elderly, and other vulnerable groups, typically suffer the heaviest burdens.";
        page1.bulletPoints = new String[]{
            "Armed conflicts take many forms: interstate wars, civil wars, insurgencies, and non-state armed confrontations.",
            "Direct impacts include battle-related deaths, traumatic injuries, and destruction of health and water/sanitation systems; indirect impacts include increased disease, malnutrition, and long-term mental-health problems.",
            "International humanitarian law (IHL) applies during armed conflict to protect civilians and set rules for conduct of hostilities, but civilian protection often remains inadequate in practice. ",
        };
        page1.imageResource = R.drawable.armed_conflict_safety_poster;
        pages.add(page1);
        
        // Page 2: Situational Awareness
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of civil and disturbance can help prevent them:";
        page2.bulletPoints = new String[]{
            "Political grievances and governance failures (power disputes, exclusion, weak institutions).",
            "Identity and social fractures (ethnic, religious, or communal tensions).",
            "Economic drivers (competition over resources, poverty, unemployment) that create incentives for mobilization.",
            "External influences (foreign interventions, arms flows, proxy dynamics).",
            "Breakdown of rule of law and lack of conflict-resolution channels, which allow disputes to escalate into violence.",
        };
        page2.imageResource = R.drawable.armed_conflict_safety_poster;
        pages.add(page2);
        
        // Page 3: Avoidance and Prevention
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "for Individuals";
        page3.content = "Follow these essential safety tips to protect yourself in case of armed conflict:";
        page3.bulletPoints = new String[]{
            "Follow official guidance and alerts from local authorities, humanitarian agencies, or trusted news sources; register with any available local protection/evacuation systems.",
            "If conflict is imminent near you: identify safe shelter locations (ideally underground/inside reinforced structures), plan evacuation routes, and keep important documents and a go-bag ready (water, food, medicines, torch, radio, cash).",
            "Avoid demonstrations and checkpoints; do not take sides publicly; keep a low profile to reduce risk.",
            "First aid knowledge is essential — learn basic trauma first aid (control bleeding, treat shock) and carry a basic medical kit if safe to do so.",
            "Protect communications and privacy: have contingency communication plans (battery-powered radio, offline contacts), and avoid sharing sensitive location details on public channels.",
            "Seek psychosocial support after exposure — conflict survivors commonly face anxiety, depression, and PTSD; early mental-health care helps recovery.",
        };
        page3.imageResource = R.drawable.armed_conflict_safety_poster;
        pages.add(page3);

        // Page 5: References
        SafetyTipPage page5 = new SafetyTipPage();
        page5.title = "References";
        page5.subtitle = "";
        page5.content = "";
        page5.bulletPoints = new String[]{
                "AUN News. (2024, September 20). Understanding armed conflict: Causes and impact. AUN News. https://aunetwork.press/understanding-armed-conflict-causes-and-impact/ AU Network",
                "Author(s). (n.d.). Causes of armed conflict. ResearchGate. https://www.researchgate.net/publication/363893986_Causes_of_Armed_Conflict ResearchGate",
                "International Committee of the Red Cross. (n.d.). What is international humanitarian law? ICRC. https://www.icrc.org/en/document/what-international-humanitarian-law ICRC",
                "Author(s). (n.d.). [Article on impacts of violence]. ScienceDirect. https://www.sciencedirect.com/science/article/pii/S0305750X24002766",
                "Paauw, D., & others. (n.d.). [Health and conflict summary article]. PubMed Central. https://pmc.ncbi.nlm.nih.gov/articles/PMC1122271/",
        };
        page5.imageResource = R.drawable.armed_conflict_safety_poster;
        pages.add(page5);

        return pages;
    }
    
    public static List<SafetyTipPage> createInfectiousDiseasePages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        // Page 1: Overview
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Overview of Infectious Disease";
        page1.subtitle = "in the Philippines";
        page1.content = "Infectious disease outbreaks occur when there is a sudden increase in cases of an illness caused by bacteria, viruses, fungi, or parasites within a specific population, community, or region. These outbreaks can range in scale from localized epidemics to widespread pandemics, as seen in COVID-19 and influenza. They disrupt healthcare systems, overwhelm resources, and cause significant social and economic consequences.";
        page1.bulletPoints = new String[]{
            "Outbreaks may be classified as endemic (regular presence of a disease in an area), epidemic (sharp rise in cases in a region), or pandemic (global spread of disease).",
            "Examples include influenza, COVID-19, Ebola, HIV/AIDS, and emerging zoonotic diseases.",
            "Common impacts include high mortality, strain on hospitals, disruption of economies, and social instability.",
        };
        page1.imageResource = R.drawable.infectious_disease_safety_poster;
        pages.add(page1);
        
        // Page 2: Basic Prevention
        SafetyTipPage page2 = new SafetyTipPage();
        page2.title = "Major Contributing Factors";
        page2.subtitle = "";
        page2.content = "Understanding the main causes of infectious disease can help prevent them:";
        page2.bulletPoints = new String[]{
            "Global travel and trade: Rapid movement of people and goods spreads pathogens across borders.",
            "Urbanization and overcrowding: Dense populations facilitate faster transmission.",
            "Environmental change: Deforestation, climate change, and animal–human interactions increase zoonotic spillover.",
            "Weak healthcare systems: Limited access to vaccines, diagnostics, and treatment worsens outbreak impact.",
            "Misinformation and poor preparedness: Lack of trust in science, vaccine hesitancy, and weak emergency planning hinder control efforts.",
        };
        page2.imageResource = R.drawable.infectious_disease_safety_poster;
        pages.add(page2);
        
        // Page 3: Vaccination and Immunity
        SafetyTipPage page3 = new SafetyTipPage();
        page3.title = "Safety Tips";
        page3.subtitle = "for Individuals";
        page3.content = "Follow these essential safety tips to protect yourself in case of infectious disease:";
        page3.bulletPoints = new String[]{
            "Practice good hygiene: Wash hands regularly, wear masks in outbreaks of airborne diseases, and follow respiratory etiquette.",
            "Stay updated: Rely on health authorities (WHO, DOH, CDC) for accurate outbreak alerts and safety protocols.",
            "Vaccinate: Stay up to date with recommended vaccines to reduce risk of severe illness.",
            "Avoid crowds during outbreaks: Limit exposure in high-risk settings when community transmission is high.",
            "Prepare a health kit: Include masks, sanitizer, basic medicines, thermometer, and personal protective items.",
            "Support mental health: Isolation and uncertainty increase stress; maintaining communication and seeking support helps."
        };
        page3.imageResource = R.drawable.infectious_disease_safety_poster;
        pages.add(page3);

        // Page 5: References
        SafetyTipPage page5 = new SafetyTipPage();
        page5.title = "References";
        page5.subtitle = "";
        page5.content = "";
        page5.bulletPoints = new String[]{
                "EBSCO Research Starters. (n.d.). Outbreaks of infectious disease. EBSCO. https://www.ebsco.com/research-starters/life-sciences/outbreaks-infectious-disease",
                "National Center for Biotechnology Information. (2017). Importance of pandemics. In Global health (Table 17.1). NCBI Bookshelf. https://www.ncbi.nlm.nih.gov/books/NBK525302/",
                "Physiopedia. (n.d.). Endemics, epidemics, and pandemics. Physiopedia. https://www.physio-pedia.com/Endemics,_Epidemics_and_Pandemics",
                "Rural Health Information Hub. (n.d.). Infectious disease outbreaks. RHIhub. https://www.ruralhealthinfo.org/toolkits/emergency-preparedness/4/infectious-disease-outbreaks",
                "World Health Organization. (n.d.). Disease outbreaks. WHO. https://www.who.int/teams/environment-climate-change-and-health/emergencies/disease-outbreaks",
        };
        page5.imageResource = R.drawable.infectious_disease_safety_poster;
        pages.add(page5);

        return pages;
    }
    
    public static List<SafetyTipPage> createDefaultPages() {
        List<SafetyTipPage> pages = new ArrayList<>();
        
        SafetyTipPage page1 = new SafetyTipPage();
        page1.title = "Safety Tips";
        page1.subtitle = "General Safety Information";
        page1.content = "General safety guidelines for your protection:";
        page1.bulletPoints = new String[]{
            "Stay informed about potential hazards in your area.",
            "Have an emergency plan and practice it regularly.",
            "Keep emergency supplies and important documents ready.",
            "Follow official safety guidelines and recommendations.",
            "Stay in contact with family and emergency services.",
            "Be aware of your surroundings and potential risks.",
            "Take appropriate safety measures for your specific situation."
        };
        page1.imageResource = 0; // No image for this page
        pages.add(page1);
        
        return pages;
    }
    
    // Data class for safety tip pages
    public static class SafetyTipPage {
        public String title;
        public String subtitle;
        public String content;
        public String[] bulletPoints;
        public int imageResource; // Resource ID for the image
    }
}


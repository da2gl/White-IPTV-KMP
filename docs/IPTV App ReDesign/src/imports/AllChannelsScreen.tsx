import svgPaths from "./svg-wpe0l72j7f";
import imgBackground from "figma:asset/759d3750eee66f589126389941b905ac90ac3b99.png";
import imgBackground1 from "figma:asset/ef2807c1bb18a0f4cbad849092392ab0661c0979.png";
import imgBackground2 from "figma:asset/e6a62ac6e013a48d6004e45602709bd9e456479a.png";
import imgBackground3 from "figma:asset/f36484c90ba2e94f67b15041db8832a61a2eae54.png";
import imgBackground4 from "figma:asset/ee74c3ec1448d0e13d195cb7b06e6b88e0e61f77.png";
import imgBackground5 from "figma:asset/16a6ae66f2f69ae37cac1e23933c51e671f78c1d.png";

function Link({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="flex-[1_0_0] min-h-px min-w-px relative self-stretch">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col gap-[4px] items-center justify-end relative size-full">{children}</div>
    </div>
  );
}

function Wrapper2({ children }: React.PropsWithChildren<{}>) {
  return (
    <div aria-hidden="true" className="absolute inset-0 pointer-events-none rounded-[12px]">
      <div className="absolute bg-[#1e293b] inset-0 rounded-[12px]" />
      <div className="absolute inset-0 overflow-hidden rounded-[12px]">{children}</div>
    </div>
  );
}

function Container3({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 size-[18px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 18 18">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function Wrapper1({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="h-[14.25px] relative shrink-0 w-[15px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 15 14.25">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function Wrapper({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="h-[4.167px] relative shrink-0 w-[8.333px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 8.33333 4.16667">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function Container2() {
  return (
    <Wrapper>
      <path d={svgPaths.p33ff77c0} fill="var(--fill-0, #CBD5E1)" id="Icon" />
    </Wrapper>
  );
}

function Container1() {
  return (
    <Wrapper1>
      <path d={svgPaths.p1755bb80} fill="var(--fill-0, #2BADEE)" id="Icon" />
    </Wrapper1>
  );
}
type ContainerTextProps = {
  text: string;
};

function ContainerText({ text }: ContainerTextProps) {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0 w-full">
      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[16px] w-full">
        <p className="leading-[24px]">{text}</p>
      </div>
    </div>
  );
}

function Container() {
  return (
    <Wrapper1>
      <path d={svgPaths.p1755bb80} fill="var(--fill-0, white)" id="Icon" />
    </Wrapper1>
  );
}

export default function AllChannelsScreen() {
  return (
    <div className="bg-[#101c22] content-stretch flex flex-col items-start relative size-full" data-name="All Channels Screen">
      <div className="h-[893px] min-h-[893px] relative shrink-0 w-full" data-name="Container">
        <div className="absolute content-stretch flex flex-col items-start left-0 pb-[15px] right-0 top-[124px]" data-name="Main">
          <div className="h-[679px] relative shrink-0 w-full" data-name="Image Grid - Channel List">
            <div className="absolute content-stretch flex flex-col gap-[10px] inset-[16px_203px_458px_16px] items-start" data-name="Container">
              <div className="aspect-square relative rounded-[12px] shrink-0 w-full" data-name="Background">
                <Wrapper2>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground} />
                </Wrapper2>
                <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.4)] content-stretch flex items-center justify-center opacity-0 right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <ContainerText text="Channel One" />
            </div>
            <div className="absolute content-stretch flex flex-col gap-[10px] inset-[16px_16px_458px_203px] items-start" data-name="Container">
              <div className="aspect-square relative rounded-[12px] shrink-0 w-full" data-name="Background">
                <Wrapper2>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground1} />
                </Wrapper2>
                <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.4)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container1 />
                </div>
              </div>
              <ContainerText text="Channel Two" />
            </div>
            <div className="absolute content-stretch flex flex-col gap-[10px] inset-[237px_203px_237px_16px] items-start" data-name="Container">
              <div className="aspect-square relative rounded-[12px] shrink-0 w-full" data-name="Background">
                <Wrapper2>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground2} />
                </Wrapper2>
                <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.4)] content-stretch flex items-center justify-center opacity-0 right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <ContainerText text="Channel Three" />
            </div>
            <div className="absolute content-stretch flex flex-col gap-[10px] inset-[237px_16px_237px_203px] items-start" data-name="Container">
              <div className="aspect-square relative rounded-[12px] shrink-0 w-full" data-name="Background">
                <Wrapper2>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground3} />
                </Wrapper2>
                <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.4)] content-stretch flex items-center justify-center opacity-0 right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <ContainerText text="Channel Four" />
            </div>
            <div className="absolute content-stretch flex flex-col gap-[10px] inset-[458px_203px_16px_16px] items-start" data-name="Container">
              <div className="aspect-square relative rounded-[12px] shrink-0 w-full" data-name="Background">
                <Wrapper2>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground4} />
                </Wrapper2>
                <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.4)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container1 />
                </div>
              </div>
              <ContainerText text="Channel Five" />
            </div>
            <div className="absolute content-stretch flex flex-col gap-[10px] inset-[458px_16px_16px_203px] items-start" data-name="Container">
              <div className="aspect-square relative rounded-[12px] shrink-0 w-full" data-name="Background">
                <Wrapper2>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgBackground5} />
                </Wrapper2>
                <div className="absolute backdrop-blur-[2px] bg-[rgba(0,0,0,0.4)] content-stretch flex items-center justify-center opacity-0 right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <ContainerText text="Channel Six" />
            </div>
          </div>
        </div>
        <div className="absolute backdrop-blur-[2px] bg-[rgba(16,28,34,0.8)] content-stretch flex flex-col items-start left-0 right-0 top-0" data-name="Header - Top App Bar">
          <div className="relative shrink-0 w-full" data-name="Container">
            <div className="flex flex-row items-center size-full">
              <div className="content-stretch flex items-center p-[16px] relative w-full">
                <div className="content-stretch flex flex-[1_0_0] flex-col items-start min-h-px min-w-px relative" data-name="Heading 1">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold justify-center leading-[0] not-italic relative shrink-0 text-[20px] text-white tracking-[-0.3px] w-full">
                    <p className="leading-[25px]">All Channels</p>
                  </div>
                </div>
                <div className="content-stretch flex items-center justify-end relative shrink-0" data-name="Container">
                  <div className="content-stretch flex items-center justify-center max-w-[480px] overflow-clip relative rounded-[9999px] shrink-0 size-[40px]" data-name="Button">
                    <Container3>
                      <path d={svgPaths.p8a35e00} fill="var(--fill-0, #CBD5E1)" id="Icon" />
                    </Container3>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="h-[52px] overflow-clip relative shrink-0 w-full" data-name="Chips / Filters">
            <div className="absolute bg-[#2badee] content-stretch flex gap-[8px] h-[36px] items-center justify-center left-[16px] pl-[16px] pr-[12px] rounded-[8px] top-0" data-name="Button">
              <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-center text-white w-[16.98px]">
                  <p className="leading-[21px]">All</p>
                </div>
              </div>
              <Wrapper>
                <path d={svgPaths.p33ff77c0} fill="var(--fill-0, white)" id="Icon" />
              </Wrapper>
            </div>
            <div className="absolute bg-[rgba(30,41,59,0.6)] content-stretch flex gap-[8.01px] h-[36px] items-center justify-center left-[96.98px] pl-[16px] pr-[12px] rounded-[8px] top-0" data-name="Button">
              <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] text-center w-[48.31px]">
                  <p className="leading-[21px]">Movies</p>
                </div>
              </div>
              <Container2 />
            </div>
            <div className="absolute bg-[rgba(30,41,59,0.6)] content-stretch flex gap-[8px] h-[36px] items-center justify-center left-[209.3px] pl-[16px] pr-[12px] rounded-[8px] top-0" data-name="Button">
              <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] text-center w-[43.98px]">
                  <p className="leading-[21px]">Sports</p>
                </div>
              </div>
              <Container2 />
            </div>
            <div className="absolute bg-[rgba(30,41,59,0.6)] content-stretch flex gap-[8.01px] h-[36px] items-center justify-center left-[317.28px] pl-[16px] pr-[12px] rounded-[8px] top-0" data-name="Button">
              <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] text-center w-[37.73px]">
                  <p className="leading-[21px]">News</p>
                </div>
              </div>
              <Container2 />
            </div>
            <div className="absolute bg-[rgba(30,41,59,0.6)] content-stretch flex gap-[8px] h-[36px] items-center justify-center left-[419.02px] pl-[16px] pr-[12px] rounded-[8px] top-0" data-name="Button">
              <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] text-center w-[29.53px]">
                  <p className="leading-[21px]">Kids</p>
                </div>
              </div>
              <Container2 />
            </div>
          </div>
        </div>
        <div className="absolute content-stretch flex flex-col items-start left-0 right-0 top-[818px]" data-name="Footer - Bottom Navigation Bar">
          <div className="backdrop-blur-[2px] bg-[rgba(16,28,34,0.8)] h-[75px] relative shrink-0 w-full" data-name="Overlay+HorizontalBorder+OverlayBlur">
            <div aria-hidden="true" className="absolute border-[#1e293b] border-solid border-t inset-0 pointer-events-none" />
            <div className="flex flex-row justify-center size-full">
              <div className="content-stretch flex items-start justify-center pb-[12px] pt-[9px] px-[8px] relative size-full">
                <Link>
                  <div className="content-stretch flex h-[32px] items-center justify-center relative rounded-[9999px] shrink-0 w-[64px]" data-name="Container">
                    <div className="h-[18px] relative shrink-0 w-[16px]" data-name="Container">
                      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 18">
                        <g id="Container">
                          <path d={svgPaths.p12a32500} fill="var(--fill-0, #94A3B8)" id="Icon" />
                        </g>
                      </svg>
                    </div>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[18px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] tracking-[0.18px] w-[34.63px]">
                      <p className="leading-[18px]">Home</p>
                    </div>
                  </div>
                </Link>
                <Link>
                  <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex h-[32px] items-center justify-center relative rounded-[9999px] shrink-0 w-[64px]" data-name="Overlay">
                    <Container3>
                      <path d={svgPaths.p186f5ba0} fill="var(--fill-0, #2BADEE)" id="Icon" />
                    </Container3>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[18px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[12px] tracking-[0.18px] w-[75.84px]">
                      <p className="leading-[18px]">All Channels</p>
                    </div>
                  </div>
                </Link>
                <Link>
                  <div className="content-stretch flex h-[32px] items-center justify-center relative rounded-[9999px] shrink-0 w-[64px]" data-name="Container">
                    <div className="h-[19px] relative shrink-0 w-[20px]" data-name="Container">
                      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 19">
                        <g id="Container">
                          <path d={svgPaths.p3e30af00} fill="var(--fill-0, #94A3B8)" id="Icon" />
                        </g>
                      </svg>
                    </div>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[18px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] tracking-[0.18px] w-[54.22px]">
                      <p className="leading-[18px]">Favorites</p>
                    </div>
                  </div>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}